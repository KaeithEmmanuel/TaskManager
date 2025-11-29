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
                git branch: 'master', url: 'https://github.com/KaeithEmmanuel/TaskManager'
            }
        }
        stage('Package (Maven)') {
            steps {
                bat "mvn -B -DskipTests package"
            }
        }

        stage('Build Docker Image') {
                    steps {
                        bat "docker build -t ${FULL_IMAGE} ."
                    }
                }

                stage('Run Docker Container (local) - optional') {
                    steps {
                        script {
                            // Uncomment to run locally on the Jenkins agent (useful for quick smoke)
                            // Make sure to stop/remove any existing container with same name before running.
                            // bat "docker rm -f ${APP_NAME} || exit 0"
                            // bat "docker run -d --name ${APP_NAME} -p ${APP_PORT}:${APP_PORT} ${FULL_IMAGE}"
                            echo "Skipped local run. Uncomment docker run lines in the Jenkinsfile if you want to run the container on the agent."
                        }
                    }
                }

                stage('K8s Container Deployment') {
                    steps {
                        script {
                            withEnv(["KUBECONFIG=${KUBECONFIG}"]) {
                                // Render templates to concrete manifests (requires envsubst to be available on the agent)
                                // template placeholders should use ${VAR_NAME} style (e.g. ${FULL_IMAGE}, ${APP_NAME}, ${APP_PORT}, ${REPLICA_COUNT})
                                bat "envsubst < ${K8S_DIR}/namespace-template.yaml > ${K8S_DIR}/namespace.yaml"
                                bat "envsubst < ${K8S_DIR}/deployment-template.yaml > ${K8S_DIR}/deployment.yaml"
                                bat "envsubst < ${K8S_DIR}/service-template.yaml > ${K8S_DIR}/service.yaml"

                                // Apply manifests (disable strict validation to allow flexible templates)
                                bat "kubectl apply -f ${K8S_DIR}/namespace.yaml --validate=false"
                                bat "kubectl apply -f ${K8S_DIR}/deployment.yaml -n ${APP_NAMESPACE} --validate=false"
                                bat "kubectl apply -f ${K8S_DIR}/service.yaml -n ${APP_NAMESPACE} --validate=false"

                                // Ensure deployment uses the built image (use set image to be safe)
                                bat "kubectl set image deployment/${APP_NAME}-deployment ${APP_NAME}=${FULL_IMAGE} -n ${APP_NAMESPACE} --record"

                                // Wait for rollout
                                bat "kubectl rollout status deployment/${APP_NAME}-deployment -n ${APP_NAMESPACE} --timeout=120s"
                            }
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
