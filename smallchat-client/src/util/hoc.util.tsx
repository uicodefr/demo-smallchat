import React from 'react';
import { GlobalInfoContext } from '../context/GlobalInfoContext';
import { UserContext } from '../context/UserContext';
import { WebSocketContext } from '../context/WebSocketContext';

export function withContext(WrappedComponent, MyContext, contextName) {
  return function (props) {
    const myContext = React.useContext(MyContext);
    const wrappedProps = { ...props, [contextName]: myContext }
    return React.createElement(WrappedComponent, wrappedProps);
  };
}

export function withAutoContext(WrappedComponent, contextParamArray) {
  return function (props) {
    if (!contextParamArray || contextParamArray.length === 0) {
      console.error('Technical error : the contextParamObject for withAutoContext is invalid');
      return null;
    }

    const wrappedProps = { ...props };

    // XXX Ugly but we CANT use 'React.useContext' in a loop and outside a function component
    // We need to modify this method when adding/deleting context
    if (contextParamArray.includes('globalInfoContext')) {
      wrappedProps['globalInfoContext'] = React.useContext(GlobalInfoContext);
    }
    if (contextParamArray.includes('userContext')) {
      wrappedProps['userContext'] = React.useContext(UserContext);
    }
    if (contextParamArray.includes('webSocketContext')) {
      wrappedProps['webSocketContext'] = React.useContext(WebSocketContext);
    }

    return React.createElement(WrappedComponent, wrappedProps);
  };
}
