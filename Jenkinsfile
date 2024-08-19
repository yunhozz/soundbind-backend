pipeline {
    agent any

    environment {
        GIT_BRANCH = 'master'
        GIT_URL = 'https://github.com/yunhozz/soundbind-backend.git'

        DOCKER_COMPOSE_FILE = 'docker-compose-dev.yml'
        PATH = "/usr/local/bin:$PATH"

        AWS_CREDENTIAL_NAME = 'AKIA44Y6CG32YILLPVC3'
        AWS_REGION = 'ap-northeast-2'
        AWS_ECR_REGISTRY = '886436935413.dkr.ecr.ap-northeast-2.amazonaws.com'
        AWS_ECR_REPOSITORY = 'sound-bind'
        AWS_ECS_SERVICE = 'soundbind-service'
        AWS_ECS_CLUSTER = 'soundbind-cluster'
        AWS_ECS_TASK_DEFINITION = 'soundbind-task-revision.json'
        AWS_CONTAINER_NAME = 'soundbind-container'
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
        stage('Build, Tag, and Push Images to Amazon ECR') {
            steps {
                script {
                    def services = sh(script: "docker compose -f ${DOCKER_COMPOSE_FILE} config --services", returnStdout: true).trim().split('\n')
                    services.each { service ->
                        def imageName = "${AWS_ECR_REGISTRY}/${AWS_ECR_REPOSITORY}:${service}-latest"
                        sh "docker compose -f ${DOCKER_COMPOSE_FILE} build ${service}"
                        sh "docker tag soundbind/${service}:latest ${imageName}"
                        sh "docker push ${imageName}"
                    }
                }
            }
        }
        stage('Deploy to ECS') {
            steps {
                script {
                    sh """
                    aws ecs update-service \
                        --cluster ${AWS_ECS_CLUSTER} \
                        --service ${AWS_ECS_SERVICE} \
                        --force-new-deployment
                    """
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