pipeline {
    agent any

    tools {
        maven 'Maven3'
    }

    triggers {
        pollSCM('* * * * *')   // check GitHub for new commits every minute
    }

    options {
        skipDefaultCheckout()
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    def scmVars = checkout scm
                    env.GIT_COMMIT = scmVars.GIT_COMMIT
                }
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B clean test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B package -DskipTests'
            }
        }

        stage('Archive') {
            steps {
                script {
                    env.VERSIONED_JAR = "status-monitor-1.0.${env.BUILD_NUMBER}-${env.GIT_COMMIT.substring(0, 7)}.jar"
                }
                sh 'cp target/status-monitor-1.0.jar "target/$VERSIONED_JAR"'
                archiveArtifacts artifacts: 'target/status-monitor-*.jar', fingerprint: true
            }
        }
    }
}
