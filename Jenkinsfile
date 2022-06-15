def generateStage(releaseJob, awsAccessKey, awsSecretKey, awsAccessToken, lzId, lzShortName, lzSchedule) {
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
            build job: "${releaseJob}", parameters: listParams, propagate: false
            println currentBuild.currentResult
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

def convertStringToList(string) {
    return Arrays.asList(string.split("\\s*,\\s*"))
}

def populateChoices(choices) {
  return '''
if (ENVIRONMENT == ('test')) { 
    return $choices
}
else if (ENVIRONMENT == ('nonprod')) {
    return ["nonprod_lzs.csv"]
}
else {
    return ["ERROR]
}
'''.stripIndent()
}

def Test = ["\"aaa\"","\"bbb\"","\"fff\"","\"eee\""]
String environments = 'test\nnonprod\nprod'
String choices = populateChoices(Test)

properties([
    parameters([
        [
            $class: 'CascadeChoiceParameter', 
            choiceType: 'PT_MULTI_SELECT',
            description: 'Select Landing Zones to patch',
            filterLength: 10,
            filterable: true,
            name: 'LANDINGZONES',
            referencedParameters: 'ENVIRONMENT',
            script: [$class: 'GroovyScript',
                script: [
                    classpath: [], 
                    sandbox: true, 
                    script: choices
                ]
            ]
        ]
    ])
])
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
            name: 'Release_Job',
            defaultValue: 'test/ReleaseJob',
            description: 'Jenkins job to call',
        )
        // string(
        //     name: 'Accounts_File',
        //     defaultValue: 'landingzones.csv',
        //     description: 'File contains list of patching accounts',
        // )
        string(
            name: 'LZ_Schedule',
            defaultValue: '1970-01-01T00:01',
            description: 'When to schedule patching. THIS IS IN GMT/UTC',
            trim: true
        )
        choice(name: 'ENVIRONMENT', choices: "${environments}")
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
    }

    stages {
        stage('Execute patching on multiple landing zones') {
            steps {
                script {
                    def lzs = []
                    if ("${params.ENVIRONMENT}" == "nonprod") {
                        lzs = convertFileToList("nonprod_lzs.csv")
                        lzs.each {
                            println it
                        }
                    }
                   
                    def parallelStagesMap = [:]
                    for (lz in lzs) {
                        parallelStagesMap[lz.get(0)] = generateStage("${params.Release_Job}",
                                                          "${params.AWS_Access_Key}",
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
