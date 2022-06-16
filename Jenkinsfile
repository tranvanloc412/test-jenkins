#!/usr/bin/env groovy

def generateStage(releaseJob, awsAccessKey, awsSecretKey, awsAccessToken, lzId, lzShortName, lzSchedule) {
    def params = [
      "AWS_Access_Key" : "${awsAccessKey}",
      "AWS_Secret_Key": "${awsSecretKey}",
      "AWS_Access_Token": "${awsAccessToken}",
      "LZ_ID": "${lzId}",
      "LZ_SHORTNAME": "${lzShortName}",
      "LZ_Schedule": "${lzSchedule}"
    ]

    List listParams = []
    params.each {
        listParams.add([$class: 'StringParameterValue', name: "${it.key}", value: "${it.value}"])
    }

    return {
        stage("${lzShortName}") {
            build job: "${releaseJob}", parameters: listParams, propagate: false
            // println currentBuild.currentResult
            // if(buildJob.result != "SUCCESS") {
            //     println "Release status: ${buildJob.result}"
            // }
        }
    }
}

def getLzsInfo(file) {
    String fileContents = readFile "${env.WORKSPACE}/${file}"
    lines = fileContents.replaceAll("(?m)^\\s*\\r?\\n|\\r?\\n\\s*(?!.*\\r?\\n)", "")
    List accounts = []
    lines.split("\n").each {
        accounts.add(it.replaceAll("\\s","").split(",") as List)
    }
    
    return accounts
}

def getTestLzsInfo(file, chosenLzs = []) {
    String fileContents = readFile "${env.WORKSPACE}/${file}"
    lines = fileContents.replaceAll("(?m)^\\s*\\r?\\n|\\r?\\n\\s*(?!.*\\r?\\n)", "")
    List accounts = []
    if(!chosenLzs.isEmpty()) {
        lines.split("\n").each {
            List tmp = it.replaceAll("\\s","").split(",")
            if (chosenLzs.contains(tmp.get(0))) {
                accounts.add(tmp)
            }
        }
    }

    return accounts
}

def convertStringToList(string) {
    return Arrays.asList(string.split("\\s*,\\s*"))
}

def populateChoices(testLzs) {
  return """
if (ENVIRONMENT == ('test')) { 
    return $testLzs
}
else if (ENVIRONMENT == ('nonprod')) {
    return ['nonprod_lzs.csv']
}
else {
    return ['ERROR']
}
""".stripIndent()
}

def envs = [
    NONPROD : "nonprod",
    PROD : "prod",
    TEST : "test"
]

String environments = "test\nnonprod\nprod"

String nonprodLzFile = "nonprod_lzs.csv"

List testLzs = ["\"lz1\"","\"lz2\"","\"lz3\"","\"lz4\"","\"lz5\""]
String testLzsFile = "test_lzs.csv"
String choices = populateChoices(testLzs)

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
            script: [
                $class: 'GroovyScript',
                fallbackScript: [
                    classpath: [], 
                    sandbox: true, 
                    script: 'return ["ERROR"]'
                ],
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
                    String chosenEnv = "${params.ENVIRONMENT}"
                    String chosenLzsStr = "${params.LANDINGZONES}"
                    List patchingLzs = []
               
                    switch(chosenEnv) {
                        case envs.NONPROD:
                            if(chosenLzsStr != "") {
                                patchingLzs = getLzsInfo(nonprodLzFile)
                            }
                            break
                        case envs.PROD:
                            patchingLzs = []
                            break
                        case  envs.TEST:
                            List chosenLzs = convertStringToList(chosenLzsStr)
                            // println chosenLzs.getClass()
                            patchingLzs = getTestLzsInfo(testLzsFile, chosenLzs)
                            break
                        default:
                            patchingLzs = []
                            break
                    }

                    patchingLzs.each {
                        println it
                        // println it.getClass()
                    }
                    // println patchingLzs.getClass()
                   
                    def parallelStagesMap = [:]
                    for (lz in patchingLzs) {
                        // println lz.get(0)
                        // println lz.get(0).getClass()
                        parallelStagesMap[lz.get(0)] = generateStage("${params.Release_Job}",
                                                          "${params.AWS_Access_Key}",
                                                          "${params.AWS_Secret_Key}",
                                                          "${params.AWS_Access_Token}",
                                                          lz.get(0),
                                                          lz.get(1),
                                                          "${params.LZ_Schedule}"
                                                      )
                    }
                    parallel parallelStagesMap
                    if (parallelStagesMap.find { it.getValue().getResult().equals('SUCCESS') }) {
                        currentBuild.result = 'SUCCESS'
                        echo "SUCCESS 123"
                    } else if (parallelStagesMap.find { it.getValue().getResult().equals('FAILURE') }) {
                        currentBuild.result = 'FAILURE'
                        echo 'FAILURE'
                    }
                }
            }
        }
    }
}
