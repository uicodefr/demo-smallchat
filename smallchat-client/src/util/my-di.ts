export interface Type<T> extends Function {
  new (...args: any[]): T;
}

class MyDi {
  private instantiationInProgress = new Set<string>();
  private classesMap = new Map<string, Type<any>>();
  private instancesMap = new Map<string, any>();

  public clear(): void {
    this.instantiationInProgress = new Set<string>();
    this.classesMap = new Map<string, Type<any>>();
    this.instancesMap = new Map<string, any>();
  }

  public register<A>(className: string, classType: Type<A>): void {
    this.classesMap.set(className, classType);
    this.instancesMap.delete(className);
  }

  public unregister(className: string): void {
    this.classesMap.delete(className);
    this.instancesMap.delete(className);
  }

  public get<T>(className: string): T {
    let myInstance = this.instancesMap.get(className);
    if (myInstance) {
      return myInstance;
    }

    let myClass = this.classesMap.get(className);
    if (!myClass) {
      throw new Error('MyDI - Service not found : ' + className);
    }

    if (this.instantiationInProgress.has(className)) {
      throw new Error('MyDI - Circular dependency found : ' + className);
    }
    this.instantiationInProgress.add(className);
    myInstance = new myClass();
    this.instantiationInProgress.delete(className);

    this.instancesMap.set(className, myInstance);
    return myInstance;
  }

  public loadInstances(): void {
    for (const className of Array.from(this.classesMap.keys())) {
      this.get(className);
    }
  }
}

export const myDi = new MyDi();
