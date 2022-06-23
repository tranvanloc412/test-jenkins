#!/usr/bin/env groovy
import groovy.transform.Field;

@Field
def envs = [
    NONPROD : "nonprod",
    PROD : "prod",
    TEST : "test"
]

@Field
def files = [
    NONPROD : "nonprod_lzs.csv",
    PROD : "prod_lzs.csv",
    TEST : "test_lzs.csv"
]

String environments =  "${envs.TEST}\n${envs.NONPROD}\n${envs.PROD}"

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
            def jobBuild = build job: "${releaseJob}", parameters: listParams, propagate: false
            def rs = jobBuild.getResult()
            echo "Pathching status on ${lzShortName} is: ${rs}"
            if(rs != "SUCCESS") {
                currentBuild.result = 'UNSTABLE'
            }
        }
    }
}

def getLzsInfoFromFile(file) {
    String fileContents = readFile "${env.WORKSPACE}/${file}"
    lines = removeEmptyLines(fileContents)
    List accounts = []

    lines.split("\n").each {
        List tmp = splitStringToList(it)
        accounts.add(tmp)
    } 
    return accounts
}

def getChosenLzsInfo(file, chosenLzs = []) {
    String fileContents = readFile "${env.WORKSPACE}/${file}"
    lines = removeEmptyLines(fileContents)
    List accounts = []

    if( !chosenLzs.isEmpty() ) {
        lines.split("\n").each {
            List tmp = splitStringToList(it)
            if (chosenLzs.contains(getShortName(tmp))) {
                accounts.add(tmp)
            }
        }
    }
    return accounts
}

def getLzShortNames(file) {
    node {
        // sh 'ls'
        String fileContents = readFile "${env.WORKSPACE}/${file}"
        lines = removeEmptyLines(fileContents)
        List accounts = []

        lines.split("\n").each {
            List tmp = splitStringToList(it)
            accounts.add("\"${getShortName(tmp)}\"")
        }
        return accounts
    }
}

def convertStringToList(string) {
    return Arrays.asList(string.split("\\s*,\\s*"))
}

def getID(List lz) {
    return lz.get(0)
}

def getShortName(List lz) {
    return lz.get(1)
}

def removeEmptyLines(content) {
    return content.replaceAll("(?m)^\\s*\\r?\\n|\\r?\\n\\s*(?!.*\\r?\\n)", "")
}

def splitStringToList(string) {
    return string.replaceAll("\\s","").split(",")
}

def populateChoices() {
    def testLzs = getLzShortNames(files.TEST)
    // def testLzs = ["\"lz1\"", "\"lz2\""]

    return """
switch(ENVIRONMENT) {
    case '$envs.TEST':
        return $testLzs
    case '$envs.NONPROD':
        return ['${files.NONPROD}']
    case '$envs.PROD':
        return ['${files.PROD}']
    default:
        return ['ERROR']
}
""".stripIndent()
}

// properties([
//     parameters([
//         [
//             $class: 'CascadeChoiceParameter', 
//             choiceType: 'PT_MULTI_SELECT',
//             description: 'Select Landing Zones to patch',
//             filterLength: 1,
//             filterable: true,
//             name: 'LANDINGZONES',
//             referencedParameters: 'ENVIRONMENT',
//             script: [
//                 $class: 'GroovyScript',
//                 script: [
//                     classpath: [], 
//                     sandbox: true, 
//                     script: choices
//                 ]
//             ]
//         ]
//     ])
// ])

pipeline {
    agent {
        label 'tooling'
    }
    // parameters {
    //     string(
    //         name: 'AWS_Access_Key',
    //         defaultValue: '',
    //         description: 'Your AWS Access Key for HIPCMSProvisionSpokeRole on CMS HUB account',
    //         trim: true
    //     )
    //     string(
    //         name: 'AWS_Secret_Key',
    //         defaultValue: '',
    //         description: 'Your AWS Secret Key for HIPCMSProvisionSpokeRole on CMS HUB account',
    //         trim: true
    //     )
    //     string(
    //         name: 'AWS_Access_Token',
    //         defaultValue: '',
    //         description: 'Your AWS Token for HIPCMSProvisionSpokeRole on CMS HUB account',
    //         trim: true
    //     )
    //     string(
    //         name: 'Release_Job',
    //         defaultValue: 'test/ReleaseJob',
    //         description: 'Jenkins job to call',
    //     )
    //     string(
    //         name: 'LZ_Schedule',
    //         defaultValue: '1970-01-01T00:01',
    //         description: 'When to schedule patching. THIS IS IN GMT/UTC',
    //         trim: true
    //     )
    //     choice(name: 'ENVIRONMENT', choices: "${environments}")
    // }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
    }


    stages {
        // stage('init') {
        //     steps {
        //         script {
        //             def choices = populateChoices()
        //         }
        //     }
        // }

        stage('Parameters') {
            steps {
                script {
                    properties([
                        parameters([
                            string(
                                name: 'AWS_Access_Key',
                                defaultValue: '',
                                description: 'Your AWS Access Key for HIPCMSProvisionSpokeRole on CMS HUB account',
                                trim: true
                            ),
                            string(
                                name: 'AWS_Secret_Key',
                                defaultValue: '',
                                description: 'Your AWS Secret Key for HIPCMSProvisionSpokeRole on CMS HUB account',
                                trim: true
                            ),
                            string(
                                name: 'AWS_Access_Token',
                                defaultValue: '',
                                description: 'Your AWS Token for HIPCMSProvisionSpokeRole on CMS HUB account',
                                trim: true
                            ),
                            string(
                                name: 'Release_Job',
                                defaultValue: 'test/ReleaseJob',
                                description: 'Jenkins job to call',
                            ),
                            string(
                                name: 'LZ_Schedule',
                                defaultValue: '1970-01-01T00:01',
                                description: 'When to schedule patching. THIS IS IN GMT/UTC',
                                trim: true
                            ),
                            choice(name: 'ENVIRONMENT', choices: "${environments}"),
                            [
                                $class: 'CascadeChoiceParameter', 
                                choiceType: 'PT_MULTI_SELECT',
                                description: 'Select Landing Zones to patch',
                                filterLength: 1,
                                filterable: true,
                                name: 'LANDINGZONES',
                                referencedParameters: 'ENVIRONMENT',
                                script: [
                                    $class: 'GroovyScript',
                                    script: [
                                        classpath: [], 
                                        sandbox: true, 
                                        script: populateChoices()
                                    ]
                                ]
                            ]
                        ])
                    ])
                }
            }
        }

        stage('Execute patching on multiple landing zones') {
            steps {
                script {
                    String chosenEnv = "${params.ENVIRONMENT}"
                    String chosenLzsStr = "${params.LANDINGZONES}"
                    List patchingLzs = []
               
                    switch(chosenEnv) {
                        case envs.NONPROD:
                            if(chosenLzsStr != "") {
                                patchingLzs = getLzsInfoFromFile(files.NONPROD)
                            }
                            break
                        case envs.PROD:
                            patchingLzs = []
                            break
                        case envs.TEST:
                            List chosenLzs = convertStringToList(chosenLzsStr)
                            patchingLzs = getChosenLzsInfo(files.TEST, chosenLzs)
                            break
                        default:
                            break
                    }

                    println "Landing Zones to be patched"
                    patchingLzs.each {
                        println it
                    }
                   
                    def parallelStagesMap = [:]
                    for (lz in patchingLzs) {
                        parallelStagesMap[getID(lz)] = generateStage("${params.Release_Job}",
                                                          "${params.AWS_Access_Key}",
                                                          "${params.AWS_Secret_Key}",
                                                          "${params.AWS_Access_Token}",
                                                          getID(lz),
                                                          getShortName(lz),
                                                          "${params.LZ_Schedule}"
                                                      )
                    }
                    parallel parallelStagesMap
                }
            }
        }
    }
}
