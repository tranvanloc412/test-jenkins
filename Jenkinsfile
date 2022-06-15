#!/usr/bin/env groovy

def generateStage(releaseJob, awsAccessKey, awsSecretKey, awsAccessToken, lzId, lzShortName, lzSchedule) {
    List params = [
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
            println currentBuild.currentResult
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
            if (chosenLzs.contains(it)) {
                accounts.add(it.replaceAll("\\s","").split(",") as List)
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

List testLzs = ["\"lz1\"","\"lz2\"","\"lz3\"","\"lz4\"","\"lz5\""]
String environments = "test\nnonprod\nprod"
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
                    List lzs = []
                    switch("${params.ENVIRONMENT}") {
                        case "nonprod":
                            // println "${params.LANDINGZONES}"
                            lzs = getLzsInfo("nonprod_lzs.csv")
                            break
                        case "prod":
                            lzs = []
                            break
                        case "test":
                            List chosenLzs = convertStringToList("${params.LANDINGZONES}")
                            // println chosenLzs
                            lzs = getTestLzsInfo("test_lzs.csv", chosenLzs)
                            break
                        default:
                            lzs = []
                            break
                    }

                    lzs.each {
                        println it
                    }
                   
                    // def parallelStagesMap = [:]
                    // for (lz in lzs) {
                    //     parallelStagesMap[lz.get(0)] = generateStage("${params.Release_Job}",
                    //                                       "${params.AWS_Access_Key}",
                    //                                       "${params.AWS_Secret_Key}",
                    //                                       "${params.AWS_Access_Token}",
                    //                                       lz.get(0),
                    //                                       lz.get(1),
                    //                                       "${params.LZ_Schedule}"
                    //                                   )
                    // }
                    // parallel parallelStagesMap
                }
            }
        }
    }
}
