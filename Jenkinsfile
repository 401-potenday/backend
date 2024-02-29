pipeline {
    agent any
    environment {
        DOCKER_IMAGE = 'potenday-dev'
        ECR_REPOSITORY = '276943215323.dkr.ecr.ap-northeast-2.amazonaws.com'
        TARGET_HOST = 'kmss6905@dev.itthatcat.xyz'
    }
    
    stages {
        
        stage('BE - PR Test before merge') {
            when {
                branch 'PR-*'
            }
            steps {
                script {
                    sh '''
                        ./gradlew test
                    '''
                }
            }
        }

        stage('BE - Test') {
            when {
                not {
                    anyOf {
                        branch 'PR-*'
                        branch 'devops'
                        branch pattern: "feature/*", comparator: "ANT"
                    }
                }
            }
            steps {
                script {
                    sh '''
                        ./gradlew test
                    '''
                }
            }
        }

        stage('BE - Build jar file') {
            when {
                allOf {
                    branch 'develop'
                    branch 'main'
                }
            }
            steps {
                script {
                    sh '''
                        ./gradlew build --exclude-task=test
                    '''
                }
            }
        }

        stage('BE - Docker Build Image') {
            steps {
                script {
                    def image = docker.build("${env.DOCKER_IMAGE}")
                    env.DOCKER_IMAGE_ID = image.id
                }
            }
        }
        
        stage('BE - Docker Push Image to ECR Dev') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    def image = docker.image("${env.DOCKER_IMAGE_ID}")
                    docker.withRegistry("https://${env.ECR_REPOSITORY}", "ecr:ap-northeast-2:potenday-ecr-credentials") {
                        image.push("${env.BUILD_NUMBER}")
                        image.push("latest")
                    }
                }
            }
        }

        stage("BE - Deploy in Dev") {
            when {
                branch 'develop'
            }
            steps {
                sshagent(credentials: ['jenkins-deploy-dev-server-credentials']) {
                    sh 'ssh -o StrictHostKeyChecking=no ${TARGET_HOST} '
                    sh "ssh ${TARGET_HOST} 'aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin ${ECR_REPOSITORY}'"
                    sh "ssh ${TARGET_HOST} 'docker compose pull && docker compose up -d'"
                }
            }
        }
    }
}
