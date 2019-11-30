import React from 'react';
import './Login.scss';
import Card from 'react-bootstrap/Card';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import ProgressBar from 'react-bootstrap/ProgressBar';
import { UserContextType } from '../context/UserContext';
import { withAutoContext } from '../util/hoc.util';
import { UserApi } from '../api/user.api';
import { GlobalInfoContextType } from '../context/GlobalInfoContext';
import { AlertType } from '../const/alert-type.const';
import { Redirect } from 'react-router-dom';
import { Formik, FormikValues } from 'formik';
import * as yup from 'yup';

interface Props {
  userContext: UserContextType,
  globalInfoContext: GlobalInfoContextType
}

interface State {
  loginInProgress: boolean,
  redirectAfterLogin: boolean
}

class Login extends React.Component<Props, State> {

  private userApi: UserApi;

  constructor(props: Props) {
    super(props);
    this.state = {
      loginInProgress: false,
      redirectAfterLogin: false
    };

    this.userApi = new UserApi(props.globalInfoContext);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleClickLogout = this.handleClickLogout.bind(this);
  }

  componentDidMount() {
    this.setState({
      redirectAfterLogin: false
    });
  }

  handleSubmit(formValues : FormikValues) {
    this.setState({
      loginInProgress: true
    });

    const username = formValues.username;
    const password = formValues.password;
    this.userApi.login(username, password).then(user => {
      this.setState({
        loginInProgress: false
      });

      if (user) {
        this.props.userContext.setCurrentUser(user);
        this.setState({
          redirectAfterLogin: true
        })
      } else {
        this.props.globalInfoContext.showAlert(AlertType.WARNING, 'Sign-in Failed : Incorrect username or password');
      }
    });
  }

  handleClickLogout(event) {
    this.userApi.logout().then(() => {
      this.props.userContext.setCurrentUser(null);
    });
  }

  render() {
    if (this.state.redirectAfterLogin) {
      return (
        <Redirect to="/" />
      );
    }

    const schema = yup.object({
      username: yup.string().required(),
      password: yup.string().required()
    });
    const initialValues = {username: '', password: ''};

    return (
      <div className="LoginScreen">
        <h1>Sign in</h1>
        <Card body>
          {!this.props.userContext.currentUser ? (
            <Formik
              validationSchema={schema}
              onSubmit={this.handleSubmit}
              initialValues={initialValues}
              >
              {({
                values,
                handleChange,
                handleBlur,
                handleSubmit
              }) => (
              <Form className="loginForm" onSubmit={handleSubmit}>
                <Form.Group>
                  <Form.Label>Username</Form.Label>
                  <Form.Control name="username" type="text" required
                    value={values.username} onChange={handleChange} onBlur={handleBlur} />
                </Form.Group>
                <Form.Group>
                  <Form.Label>Password</Form.Label>
                  <Form.Control name="password" type="password" required
                    value={values.password} onChange={handleChange} onBlur={handleBlur} />
                </Form.Group>
                {!this.state.loginInProgress ? (
                  <div className="buttonZone">
                    <Button variant="primary" type="submit"> LOGIN </Button>
                  </div>
                ) : (
                    <ProgressBar animated now={100} />
                  )}
              </Form>)}
            </Formik>
          ) : (
              <div className="alreadyConnected">
                You are connected as <em>{this.props.userContext.currentUser.username}</em>
                <br />
                <Button variant="danger" type="button" onClick={this.handleClickLogout}> LOGOUT </Button>
              </div>
            )}
        </Card>

        <div className="help">
          For the demo use the password : password
        </div>
      </div>
    );
  }
}

export default withAutoContext(Login, ['globalInfoContext', 'userContext']);