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
            dir('../phaedra2-parent') {
            git url: 'https://scm.openanalytics.eu/git/phaedra2-parent'
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
