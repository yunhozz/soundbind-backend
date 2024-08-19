pipeline {
    agent any

    environment {
        GIT_BRANCH = 'master'
        GIT_URL = 'https://github.com/yunhozz/soundbind-backend.git'

        DOCKER_COMPOSE_FILE = 'docker-compose-dev.yml'
        PATH = "/usr/local/bin:$PATH"

        AWS_CREDENTIAL_NAME = 'AKIA44Y6CG32YILLPVC3'
        AWS_CONTAINER_NAME = 'soundbind-container'
        AWS_ECR_PATH = '886436935413.dkr.ecr.ap-northeast-2.amazonaws.com'
        AWS_ECR_IMAGE_PATH = '886436935413.dkr.ecr.ap-northeast-2.amazonaws.com/sound-bind'
        AWS_REGION = 'ap-northeast-2'
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: "${GIT_BRANCH}", url: "${GIT_URL}"
            }
        }
        stage('Build Docker Images') {
            steps {
                script {
                    sh "docker-compose -f ${DOCKER_COMPOSE_FILE} build"
                }
            }
        }
        stage('Tag and Push to ECR') {
            steps {
                script {
                    sh "docker tag ${AWS_CONTAINER_NAME}:latest ${AWS_ECR_IMAGE_PATH}:latest"
                    sh "docker tag ${AWS_CONTAINER_NAME}:latest ${AWS_ECR_IMAGE_PATH}:${BUILD_NUMBER}"

                    docker.withRegistry("https://${AWS_ECR_PATH}", "ecr:${AWS_REGION}:${AWS_CREDENTIAL_NAME}") {
                        docker.image("${AWS_ECR_IMAGE_PATH}:${BUILD_NUMBER}").push()
                        docker.image("${AWS_ECR_IMAGE_PATH}:latest").push()
                    }
                }
            }
        }
    }

    post {
        always {
            sh "docker-compose -f ${DOCKER_COMPOSE_FILE} logs"
        }
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}