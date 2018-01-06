node {
    def mvnHome
    def JAVA_HOME
    def tempDir = "/VWMC/TempWorkDir"
    def currentProject = "pubsub.subscriber.demo"
    def project
    def subscription
    def bucket

    stage('Preparation') { // for display purposes

        JAVA_HOME = tool name: 'JDK1.8-152', type: 'jdk'
        mvnHome = tool name: 'Maven-3.5.2', type: 'maven'
    }

    stage('Checkout from Github') {

        sh("rm -rf ${tempDir}/${currentProject}")
        sh("mkdir ${tempDir}/${currentProject}")

        dir("${tempDir}/${currentProject}") {
            git (branch:'dev', url:'https://github.com/innovative-innovators/pubsub.subscriber.demo.git')
        }
    }

    stage('Build') {
        dir("${tempDir}/${currentProject}") {
            sh("${mvnHome}/bin/mvn clean compile")
        }
    }

    stage('Input Required Info') {

        def inputParams = input(message: 'Required Info',
                parameters: [
                        [$class: 'StringParameterDefinition', defaultValue: '', description: '', name: 'Project Name'],
                        [$class: 'StringParameterDefinition', defaultValue: '', description: '', name: 'Subscriber Name'],
                        [$class: 'StringParameterDefinition', defaultValue: '', description: '', name: 'Bucket Name']
                ])

        project = inputParams['Project Name']
        subscription = inputParams['Subscriber Name']
        bucket = inputParams['Bucket Name']

        echo("Project is : " + project)
        echo("Subscription is : " + subscription)
        echo("Bucket is : " + bucket)
    }

    stage("Execute Subscription Stream") {
        dir("${tempDir}/${currentProject}") {
//            sh("${mvnHome}/bin/mvn -Dtest=SubscriberDemo#testSubscribe -DargLine=\"-Dproject=${project} -DsubscriberId=${subscription} -Dbucket=${bucket} -javaagent:/home/vincent_chen/.m2/repository/org/mortbay/jetty/alpn/jetty-alpn-agent/2.0.6/jetty-alpn-agent-2.0.6.jar -Xmx2048m\" test")
            sh("${mvnHome}/bin/mvn -Dtest=SubscriberDemo#testWriteToGCS -DargLine=\"-Dproject=${project} -DsubscriberId=${subscription} -Dbucket=${bucket} -javaagent:/home/vincent_chen/.m2/repository/org/mortbay/jetty/alpn/jetty-alpn-agent/2.0.6/jetty-alpn-agent-2.0.6.jar -Xmx2048m\" test")
        }
    }
}