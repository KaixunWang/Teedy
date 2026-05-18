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
                script {
                    // Use local base image cache; --pull=false avoids broken registry-mirror metadata errors
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}", '--pull=false')
                }
            }
        }
        stage('Upload image') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', env.DOCKER_HUB_CREDENTIALS) {
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
                    }
                }
            }
        }
        stage('Run containers') {
            steps {
                script {
                    ['8082', '8083', '8084'].each { port ->
                        sh "docker stop teedy-container-${port} || true"
                        sh "docker rm teedy-container-${port} || true"
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run(
                            "-d --name teedy-container-${port} -p ${port}:8080"
                        )
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
