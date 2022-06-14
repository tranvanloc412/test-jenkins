def generateStage(awsAccessKey, awsSecretKey, awsAccessToken, lzId, lzShortName, lzSchedule) {
    def params = [
      "AWS_Access_Key" : "${awsAccessKey}",
      "AWS_Secret_Key": "${awsSecretKey}",
      "AWS_Access_Token": "${awsAccessToken}",
      "LZ_ID": "${lzId}",
      "LZ_SHORTNAME": "${lzShortName}",
      "LZ_Schedule": "${lzSchedule}"
    ]
    def listParams = []
    params.each {
        listParams.add([$class: 'StringParameterValue', name: "${it.key}", value: "${it.value}"])
    }
    return {
        stage("Patching on LZ: ${lzId}") {
            build job: "test/ReleaseJob", parameters: listParams, propagate: false
        }
    }
}

pipeline {
    agent {
        label 'tooling'
    }

    parameters {
        string(
            name: 'AWS_Access_Key',
            defaultValue: '',
            description: 'Your AWS Access Key for HIPCMSProvisionSpokeRole on CMS HUB account',
            trim: true
        )
        string(
            name: 'AWS_Secret_Key',
            defaultValue: '',
            description: 'Your AWS Secret Key for HIPCMSProvisionSpokeRole on CMS HUB account',
            trim: true
        )
        string(
            name: 'AWS_Access_Token',
            defaultValue: '',
            description: 'Your AWS Token for HIPCMSProvisionSpokeRole on CMS HUB account',
            trim: true
        )
        string(
            name: 'LZ_Schedule',
            defaultValue: '1970-01-01T00:01',
            description: 'When to schedule patching. THIS IS IN GMT/UTC',
            trim: true
        )
        // file(
        //     fileLocation: 'accounts.csv',
        //     description: 'contains list of patching accounts'
        // )
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
    }

    stages {
        stage('Preparation') {
            steps{
                script {
                    String fileContents = new File('accounts.csv').getText('UTF-8')
                    echo fileContents
                }
            }
        }
        stage('Execute patching on multiple landing zones') {
            steps {
                script {
                    def lzs = [
                        [ 'lz1', 'a'],
                        [ 'lz2', 'b'],
                        [ 'lz3', 'b']
                    ]
                    def parallelStagesMap = [:]
                    for (lz in lzs) {
                        parallelStagesMap[lz.get(0)] = generateStage("${params.AWS_Access_Key}",
                                                          "${params.AWS_Secret_Key}",
                                                          "${params.AWS_Access_Token}",
                                                          lz.get(0),
                                                          lz.get(1),
                                                          "${params.LZ_Schedule}"
                                                      )
                    }
                    parallel parallelStagesMap
                }
            }
        }
    }
}
