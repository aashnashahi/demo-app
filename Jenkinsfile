pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    environment {
        APP_NAME = 'demo-app'
        IMAGE_NAME = 'demo-app'
        REGISTRY = 'localhost:5000'
        NEXUS_CREDS = credentials('nexus-creds')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B clean compile'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Sonar Connectivity Test') {
            steps {
                bat 'curl.exe -i --max-time 15 http://localhost:9000/api/system/status'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=demo-app -Dsonar.projectName=demo-app -Dsonar.host.url=%SONAR_HOST_URL% -Dsonar.token=%SONAR_AUTH_TOKEN%'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package') {
            steps {
                bat 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker build -t %IMAGE_NAME%:%BUILD_NUMBER% -t %IMAGE_NAME%:latest .'
                bat 'docker images %IMAGE_NAME%'
            }
        }

        stage('Push to Nexus') {
            steps {
                bat 'echo %NEXUS_CREDS_PSW%| docker login %REGISTRY% -u %NEXUS_CREDS_USR% --password-stdin'
                bat 'docker tag %IMAGE_NAME%:latest %REGISTRY%/%IMAGE_NAME%:%BUILD_NUMBER%'
                bat 'docker push %REGISTRY%/%IMAGE_NAME%:%BUILD_NUMBER%'
                bat 'docker logout %REGISTRY%'
            }
        }

        stage('Deploy') {
            steps {
                bat 'docker rm -f demo-app 2>nul & docker run -d --name demo-app -p 8081:8080 %REGISTRY%/%IMAGE_NAME%:%BUILD_NUMBER%'
            }
        }
    }

    post {
        success {
            echo 'Build, test, package, Docker build, Nexus push and deployment completed.'
        }
        failure {
            echo 'Pipeline failed - check the stage logs.'
        }
    }
}
