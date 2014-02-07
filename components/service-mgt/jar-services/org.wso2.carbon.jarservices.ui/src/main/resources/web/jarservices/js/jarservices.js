
function CarbonClass(){
    this.methods = new Array();
    this.setClassName = setClassName;
    this.addMethod = addMethod;
    this.getMethod = getMethod;
}

function setClassName(className){
    this.className = className;
}

function addMethod(method){
    this.methods.push(method);
}

function getMethod(methodName){
    for (var j = 0; j < this.methods.length; j++) {
        if (this.methods[j].methodName == methodName) {
            return this.methods[j];
        }
    }
    return null;
}

function CarbonMethod(){
    this.selected = true;
    this.setMethodName = setMethodName;
}

function setMethodName(methodName){
    this.methodName = methodName;
}
