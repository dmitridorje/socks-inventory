pipeline {
    agent any

    environment {
        IMAGE_NAME = 'dmitridorje/socks'
        IMAGE_TAG = 'latest'
        FULL_IMAGE = "${IMAGE_NAME}:${IMAGE_TAG}"

        JAVA_HOME = '/home/dmitridorje/.sdkman/candidates/java/current'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Check gradlew permissions') {
            steps {
                sh '''
                    echo "Checking gradlew permissions:"
                    ls -l gradlew || echo "gradlew not found!"

                    if [ ! -x gradlew ]; then
                        echo "gradlew is not executable. Applying chmod +x..."
                        chmod +x gradlew
                    else
                        echo "gradlew is already executable."
                    fi
                '''
            }
        }

        stage('Build with gradlew') {
            steps {
                echo '🧱 Building Java application...'
                sh './gradlew clean build -x test'
            }
        }

        stage('Run Tests with Testcontainers') {
            steps {
                echo '🧪 Running tests...'
                sh './gradlew test'
            }
        }

        stage('Docker Build') {
            steps {
                echo '🐳 Building Docker image...'
                sh "docker build -t ${FULL_IMAGE} ."
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials',
                                                  usernameVariable: 'DOCKER_USERNAME',
                                                  passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                    '''
                    sh "docker push ${FULL_IMAGE}"
                }
            }
        }
    }

    post {
            success {
                withCredentials([string(credentialsId: 'telegram-bot-token', variable: 'TELEGRAM_TOKEN')]) {
                    sh '''
                        curl -s -X POST https://api.telegram.org/bot$TELEGRAM_TOKEN/sendMessage \
                            -d chat_id=1646517486 \
                            -d text="✅ Jenkins: Сборка ${env.JOB_NAME} #${env.BUILD_NUMBER} прошла успешно!"
                    '''
                }
            }
            failure {
                withCredentials([string(credentialsId: 'telegram-bot-token', variable: 'TELEGRAM_TOKEN')]) {
                    sh '''
                        curl -s -X POST https://api.telegram.org/bot$TELEGRAM_TOKEN/sendMessage \
                            -d chat_id=1646517486 \
                            -d text="❌ Jenkins: Сборка ${env.JOB_NAME} #${env.BUILD_NUMBER} упала."
                    '''
                }
            }
        }
}
