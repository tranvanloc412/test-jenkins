// pipeline {
//     agent {
//       label 'tooling'
//     }
//     parameters {
//       string(name: 'FROM_BUILD', defaultValue: '', description: 'Build Source')
//       string(name: 'PARALLEL', defaultValue: '', description: 'Parallel Number')
//       booleanParam(name: 'IS_READY', defaultValue: false, description: 'Is ready for Prod?')
//     }
//     stages {
//         stage('DEPLOY') {
//             steps {
//                 echo "Deploying from source ${params.FROM_BUILD}"
//                 echo "Parallel number: ${params.PARALLEL}"
//                 sh 'exit 0'
//             }
//         }
//     }
// }

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
            name: 'LZ_ID',
            defaultValue: '643141967929',
            description: 'Landing zone ID to jump to',
            trim: true
        )
        string(
            name: 'LZ_SHORTNAME',
            defaultValue: 'cmsnonprod',
            description: 'Landing zone name to jump to',
            trim: true
        )
        string(
            name: 'LZ_Schedule',
            defaultValue: '1970-01-01T00:01',
            description: 'When to schedule patching. THIS IS IN GMT/UTC',
            trim: true
        )
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
    }

    stages {
        stage('Print Parameters') {
            steps {
                echo "AWS_Access_Key: ${params.AWS_Access_Key}"
                echo "AWS_Secret_Key: ${params.AWS_Secret_Key}"
                echo "AWS_Access_Token: ${params.AWS_Access_Token}"
                echo "LZ_ID: ${params.LZ_ID}"
                echo "LZ_SHORTNAME: ${params.LZ_SHORTNAME}"
                echo "LZ_Schedule: ${params.LZ_Schedule}"
                script {
                  if("${params.LZ_ID}" == "lz5") {
                      sh 'exit 1'
                  }
                }
            }
        }
    }
}