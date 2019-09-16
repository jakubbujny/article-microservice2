microserviceName = "microservice2"

pipeline {
    agent {
        kubernetes {
            //cloud 'kubernetes'
            label 'mypod'
            yaml """
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: cicd
  containers:
  - name: docker
    image: docker:1.11
    command: ['cat']
    tty: true
    volumeMounts:
    - name: dockersock
      mountPath: /var/run/docker.sock
  - name: kubectl
    image: ubuntu:18.04
    command: ['cat']
    tty: true
  volumes:
  - name: dockersock
    hostPath:
      path: /var/run/docker.sock
"""
        }
    }
    stages {
        stage('Build Docker image') {
            steps {
                checkout scm
                container('docker') {
                    script {
                        def image = docker.build("digitalrasta/article-${microserviceName}:${BUILD_NUMBER}")
                        docker.withRegistry( '', "dockerhub") {
                            image.push()
                        }
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                container('kubectl') {
                    script {
                        sh "apt-get update && apt-get install -y curl"
                        sh "curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.15.0/bin/linux/amd64/kubectl"
                        sh "chmod +x ./kubectl"
                        sh "mv ./kubectl /usr/local/bin/kubectl"
                        def checkDeployment = sh(script: "kubectl get deployments | grep ${microserviceName}", returnStatus: true)
                        if(checkDeployment != 0) {
                            sh "kubectl apply -f deploy/deploy.yaml"
                        }
                        sh "kubectl set image deployment/${microserviceName} ${microserviceName}=digitalrasta/article-${microserviceName}:${BUILD_NUMBER}"
                    }
                }
            }
        }
    }
}
