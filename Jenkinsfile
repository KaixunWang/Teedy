pipeline {
    agent any

    environment {
        // Jenkins credential ID (Manage Jenkins → Credentials): Username with password for Docker Hub
        DOCKER_HUB_CREDENTIALS = 'dockerhub_credentials'
        // Docker Hub repository: https://hub.docker.com/r/kaixunwangwww/teedy
        DOCKER_IMAGE = 'kaixunwangwww/teedy'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Clean') {
            steps {
                sh 'mvn clean'
            }
        }
        stage('Compile') {
            steps {
                sh 'mvn compile'
            }
        }
        stage('Test') {
            steps {
                sh '''
if command -v brew >/dev/null 2>&1; then
  brew list tesseract >/dev/null 2>&1 || brew install tesseract
  brew list tesseract-lang >/dev/null 2>&1 || brew install tesseract-lang
fi
'''
                sh 'mvn test -Dmaven.test.failure.ignore=true'
            }
        }
        stage('PMD') {
            steps {
                sh 'mvn pmd:pmd'
            }
        }
        stage('JaCoCo') {
            steps {
                sh 'mvn jacoco:report'
            }
        }
        stage('Javadoc') {
            steps {
                sh 'mvn javadoc:javadoc'
            }
        }
        stage('Site') {
            steps {
                sh 'mvn site'
            }
        }
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
        stage('Building image') {
            steps {
                sh "docker build -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} --pull=false ."
            }
        }
        stage('Upload image') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${DOCKER_HUB_CREDENTIALS}", usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                        docker push ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }
        stage('Run containers') {
            steps {
                script {
                    ['8082', '8083', '8084'].each { port ->
                        sh "docker stop teedy-container-${port} || true"
                        sh "docker rm teedy-container-${port} || true"
                        sh "docker run -d --name teedy-container-${port} -p ${port}:8080 ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                    }
                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '**/target/site/**/*.*', fingerprint: true
            archiveArtifacts artifacts: '**/target/**/*.jar', fingerprint: true
            archiveArtifacts artifacts: '**/target/**/*.war', fingerprint: true
            junit '**/target/surefire-reports/*.xml'
        }
    }
}
