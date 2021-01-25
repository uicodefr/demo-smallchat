import { myDi } from './my-di';

describe('myDi', () => {
  class A {
    getOne() {
      return 1;
    }
  }

  class B {
    getTwo() {
      return 2;
    }
  }

  beforeEach(() => {
    myDi.clear();
  });

  test('register', () => {
    expect(() => myDi.get('A')).toThrow(Error);
    expect(() => myDi.get('B')).toThrow(Error);

    myDi.register('A', A);
    myDi.register('B', B);
    const a = myDi.get<A>('A');
    const b = myDi.get<B>('B');
    expect(a.getOne()).toBe(1);
    expect(b.getTwo()).toBe(2);

    myDi.unregister('A');
    expect(() => myDi.get('A')).toThrow(Error);
  });
});
