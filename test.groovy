String environments = 'test\nnonprod\nprod'

def Test = ["\"aaa\"","\"bbb\"","\"fff\"","\"eee\""]


def populateChoice(choices) {
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

def convertStringToList(string) {
    return Arrays.asList(string.split("\\s*,\\s*"))
}

String items = populateItems(Test)

properties([
    parameters([
        [$class: 'CascadeChoiceParameter', 
            choiceType: 'PT_MULTI_SELECT',
            description: 'Select Landing Zones to patch',
            filterLength: 10,
            filterable: true,
            name: 'LANDINGZONES',
            referencedParameters: 'ENVIRONMENT',
            script: [$class: 'GroovyScript',
                // fallbackScript: [
                //     classpath: [], 
                //     sandbox: true, 
                //     script: 'return ["ERROR"]'
                // ],
                script: [
                    classpath: [], 
                    sandbox: true, 
                    script: items
                ]
            ]
        ]
    ])
])

pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        // ansiColor('xterm')
    }
    parameters {
        choice(name: 'ENVIRONMENT', choices: "${environments}")
    }
    stages {
        stage("Run Tests") {
            steps {
                sh "echo SUCCESS on ${params.ENVIRONMENT}"
                echo "result: ${params.LANDINGZONES}"
                script {
                    def lzs = convertStringToList("${params.LANDINGZONES}")
                    println lzs
                }
            }
        }
    }
}

