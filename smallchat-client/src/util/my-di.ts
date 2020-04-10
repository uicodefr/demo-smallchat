interface Type<T> extends Function {
  new (...args: any[]): T;
}

class MyDi {
  private classesMap = new Map<string, Type<any>>();
  private instancesMap = new Map<string, any>();

  public register<T>(classType: Type<T>): void {
    this.registerWithOther(classType, classType);
  }

  public registerWithOther<A, B>(keyClassType: Type<A>, classTypeToInstance: Type<B>): void {
    this.classesMap.set(keyClassType.name, classTypeToInstance);
    this.instancesMap.delete(keyClassType.name);
  }

  public get<T>(classType: Type<T>): T {
    let myInstance = this.instancesMap.get(classType.name);
    if (myInstance) {
      return myInstance;
    }

    const myClass = this.classesMap.get(classType.name);
    if (!myClass) {
      throw new Error('MyDI - Service not found : ' + classType.name);
    }
    myInstance = new myClass();
    this.instancesMap.set(classType.name, myInstance);
    return myInstance;
  }
}

export const myDi = new MyDi();
