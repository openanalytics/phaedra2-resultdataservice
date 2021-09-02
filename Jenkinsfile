pipeline {

    agent {
        kubernetes {
            yamlFile 'kubernetesPod.yaml'
        }
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '3'))
    }

    stages {

        stage('Checkout phaedra2-parent') {
            steps {
                dir('../phaedra2-parent') {
                    checkout([$class: 'GitSCM', branches: [[name: '*/develop']], extensions: [], userRemoteConfigs: [[credentialsId: 'oa-jenkins', url: 'https://scm.openanalytics.eu/git/phaedra2-parent']]])
                }
                dir('../phaedra2-commons') {
                    checkout([$class: 'GitSCM', branches: [[name: '*/develop']], extensions: [], userRemoteConfigs: [[credentialsId: 'oa-jenkins', url: 'https://scm.openanalytics.eu/git/phaedra2-commons']]])
                }
            }
        }

        stage('Build Phaedra2 commons') {
            steps {
                dir('../phaedra2-commons') {
                    container('builder') {

                        configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {

                            sh 'mvn -s $MAVEN_SETTINGS_RSB -U clean install'

                        }

                    }
                }
            }
        }

        stage('Build') {

            steps {

                container('builder') {

                    configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {

                        sh 'mvn -s $MAVEN_SETTINGS_RSB -U clean package -DskipTests'

                    }
                }
            }
        }

        stage('Test') {

            steps {

                container('builder') {

                    configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {

                        sh 'mvn -s $MAVEN_SETTINGS_RSB test'

                    }
                }
            }
        }

        stage('Build Docker image') {

            steps {

                container('builder') {

                    configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {

                        sh 'mvn -s $MAVEN_SETTINGS_RSB mvn dockerfile:build'

                    }
                }
            }
        }
    }

//    post {
//        success {
//            step([$class: 'JacocoPublisher',
//                  execPattern: 'target/jacoco.exec',
//                  classPattern: 'target/classes',
//                  sourcePattern: 'src/main/java',
//                  exclusionPattern: 'src/test*'
//            ])
//        }
//    }

}
