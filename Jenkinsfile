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
        stage("Patching ${lzId}") {
            def "job-${lzId}" = build job: "test/ReleaseJob", parameters: listParams, propagate: false
        }
        if("job-${lzId}".result != "SUCCESS") {
            echo "Release status: FAILED"
        }
    }
}

def convertFileToList(file) {
    def fileContents = readFile "${env.WORKSPACE}/${file}"
    lines = fileContents.replaceAll("(?m)^\\s*\\r?\\n|\\r?\\n\\s*(?!.*\\r?\\n)", "")
    def accounts = []
    lines.split("\n").each {
        accounts.add(it.replaceAll("\\s","").split(",") as List)
    }
    return accounts
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
        string(
            name: 'Accounts_File',
            defaultValue: 'accounts.csv',
            description: 'File contains list of patching accounts',
        )
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
    }

    stages {
        stage('Execute patching on multiple landing zones') {
            steps {
                script {
                    def lzs = convertFileToList("${params.Accounts_File}")
                    lzs.each {
                        println it
                    }
                    def parallelStagesMap = [:]
                    for (lz in lzs) {
                        parallelStagesMap[lz.get(0)] = generateStage("${params.AWS_Access_Key}",
                                                          "${params.AWS_Secret_Key}",
                                                          "${params.AWS_Access_Token}",
                                                          lz.get(0),
                                                          lz.get(1),
                                                          lz.get(2)
                                                      )
                    }
                    parallel parallelStagesMap
                }
            }
        }
    }
}
