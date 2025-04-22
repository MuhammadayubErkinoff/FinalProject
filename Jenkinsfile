pipeline {
    agent any
    stages {
        stage('Package') {
            steps {
                echo "Building...."
                sh '''
                chmod 777 mvnw
                '''
                sh '''
                ./mvnw package -DskipTests
                '''
            }
        }
        stage('Build') {
            steps {
                echo 'Build....'
                sh '''
                    docker-compose build
                '''
            }
        }
        stage('Deliver') {
            steps {
                echo 'Deliver....'
                sh '''
                docker-compose up -d
                '''
            }
        }
    }
}