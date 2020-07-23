const process = require('child_process')

console.log("starter");
console.log(process.argv[0]);
this.process = process.spawn(process.argv[2], [], {detached: true, stdio: 'ignore', shell: true, });          
this.process.unref();