import './Login.scss';
import React from 'react';
import Card from 'react-bootstrap/Card';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import ProgressBar from 'react-bootstrap/ProgressBar';
import { AlertType } from '../const/alert-type.const';
import { Redirect } from 'react-router-dom';
import { Formik, FormikValues } from 'formik';
import * as yup from 'yup';
import { AuthenticationService } from '../service/auth/authentication.service';
import { GlobalInfoService } from '../service/util/global-info.service';
import { UserModel } from '../model/global/user.model';
import { Subscription } from 'rxjs';
import { myDi } from '../util/my-di';

interface Props {}

interface State {
  currentUser: UserModel | null;
  loginInProgress: boolean;
  redirectAfterLogin: boolean;
}

export class Login extends React.Component<Props, State> {
  private authenticationService: AuthenticationService;
  private globalInfoService: GlobalInfoService;

  private currentUserSubscription: Subscription;

  constructor(props: Props) {
    super(props);

    this.authenticationService = myDi.get('AuthenticationService');
    this.globalInfoService = myDi.get('GlobalInfoService');

    this.state = {
      currentUser: this.authenticationService.getCurrentUser(),
      loginInProgress: false,
      redirectAfterLogin: false,
    };

    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleClickLogout = this.handleClickLogout.bind(this);
  }

  componentDidMount() {
    this.setState({
      redirectAfterLogin: false,
    });

    this.currentUserSubscription = this.authenticationService.getCurrentUserObservable().subscribe((currentUser) => {
      this.setState({ currentUser: currentUser });
    });
  }

  componentWillUnmount() {
    if (this.currentUserSubscription) {
      this.currentUserSubscription.unsubscribe();
    }
  }

  handleSubmit(formValues: FormikValues) {
    this.setState({
      loginInProgress: true,
    });

    const username = formValues.username;
    const password = formValues.password;
    this.authenticationService.login(username, password).then((user) => {
      this.setState({
        loginInProgress: false,
      });

      if (user) {
        this.setState({
          redirectAfterLogin: true,
        });
      } else {
        this.globalInfoService.showAlert(AlertType.WARNING, 'Sign-in Failed : Incorrect username or password');
      }
    });
  }

  handleClickLogout(event: React.MouseEvent) {
    this.authenticationService.logout();
  }

  render() {
    if (this.state.redirectAfterLogin) {
      return <Redirect to="/" />;
    }

    const schema = yup.object({
      username: yup.string().required().matches(new RegExp(UserModel.USERNAME_PATTERN)),
      password: yup.string().required(),
    });
    const initialValues = { username: '', password: '' };

    return (
      <div className="LoginScreen">
        <h1>Sign in</h1>
        <Card body>
          {!this.state.currentUser ? (
            <Formik validationSchema={schema} onSubmit={this.handleSubmit} initialValues={initialValues}>
              {({ values, errors, handleChange, handleBlur, handleSubmit }) => (
                <Form className="loginForm" onSubmit={handleSubmit}>
                  <Form.Group>
                    <Form.Label>Username</Form.Label>
                    <Form.Control
                      name="username"
                      type="text"
                      required
                      value={values.username}
                      pattern={UserModel.USERNAME_PATTERN}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={!!errors.username}
                    />
                    <Form.Control.Feedback type="invalid">{errors.username}</Form.Control.Feedback>
                  </Form.Group>
                  <Form.Group>
                    <Form.Label>Password</Form.Label>
                    <Form.Control
                      name="password"
                      type="password"
                      required
                      value={values.password}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={!!errors.password}
                    />
                    <Form.Control.Feedback type="invalid">{errors.password}</Form.Control.Feedback>
                  </Form.Group>
                  {!this.state.loginInProgress ? (
                    <div className="buttonZone">
                      <Button variant="primary" type="submit" data-testid="login-button">
                        LOGIN
                      </Button>
                    </div>
                  ) : (
                    <ProgressBar animated now={100} />
                  )}
                </Form>
              )}
            </Formik>
          ) : (
            <div className="alreadyConnected">
              You are connected as <em>{this.state.currentUser.username}</em>
              <br />
              <Button variant="danger" type="button" onClick={this.handleClickLogout} data-testid="logout-button">
                LOGOUT
              </Button>
            </div>
          )}
        </Card>

        <div className="help">For the demo use the password : password</div>
      </div>
    );
  }
}
