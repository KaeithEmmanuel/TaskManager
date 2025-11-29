pipeline {
    agent any

    environment {
        APP_NAME       = "task-manager"
        APP_NAMESPACE  = "${APP_NAME}"
        IMAGE_NAME     = "${APP_NAME}"
        IMAGE_TAG      = "${BUILD_NUMBER ?: 'local'}"
        FULL_IMAGE     = "${IMAGE_NAME}:${IMAGE_TAG}"
        K8S_DIR        = "k8s"
        KUBECONFIG     = "C:/Users/dmand/.kube/config"
        CLUSTER_TYPE   = ""          // set: kind / minikube / empty
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build (Maven)') {
            steps {
                bat "mvn -B clean package -DskipTests"
            }
        }

        stage('Build Docker Image') {
            steps {
                bat "docker build -t ${FULL_IMAGE} ."
            }
        }

        stage('K8s Deployment') {
            steps {
                script {

                    withEnv(["KUBECONFIG=${KUBECONFIG}"]) {

                        // Load image into cluster if needed
                        if (env.CLUSTER_TYPE == 'kind') {
                            bat "kind load docker-image ${FULL_IMAGE}"
                        } else if (env.CLUSTER_TYPE == 'minikube') {
                            bat "minikube image load ${FULL_IMAGE}"
                        }

                        // Apply Kubernetes manifests
                        bat "kubectl apply -f ${K8S_DIR}/namespace.yaml --validate=false"
                        bat "kubectl apply -f ${K8S_DIR}/deployment.yaml -n ${APP_NAMESPACE} --validate=false"
                        bat "kubectl apply -f ${K8S_DIR}/service.yaml -n ${APP_NAMESPACE} --validate=false"

                        // Update running deployment with new image
                        bat \"kubectl set image deployment/${APP_NAME}-deployment ${APP_NAME}=${FULL_IMAGE} -n ${APP_NAMESPACE} --record\"

                        // Rollout status check
                        bat \"kubectl rollout status deployment/${APP_NAME}-deployment -n ${APP_NAMESPACE} --timeout=120s\"
                    }
                }
            }
        }

        stage('Smoke Test') {
            steps {
                script {
                    bat """
                        kubectl -n ${APP_NAMESPACE} port-forward svc/${APP_NAME} 18080:8150 > NUL 2>&1 &
                        timeout /t 3 > NUL
                        powershell -Command "$s=(Invoke-WebRequest http://localhost:18080/api/tasks -UseBasicParsing).StatusCode; if($s -eq 200 -or $s -eq 204){ exit 0 } else { exit 1 }"
                    """
                }
            }
        }
    }

    post {
        success {
            bat "kubectl -n ${APP_NAMESPACE} get pods"
            echo "✅ Build, Dockerize & K8s Deploy completed successfully!"
        }
        failure {
            bat "kubectl -n ${APP_NAMESPACE} get pods"
            echo "❌ Pipeline failed!"
        }
    }
}
