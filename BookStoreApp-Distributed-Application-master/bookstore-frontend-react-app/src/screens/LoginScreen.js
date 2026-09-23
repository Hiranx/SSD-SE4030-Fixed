import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Form, Button, Row, Col } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import Message from '../components/Message';
import { login } from '../actions/userActions';
import FormContainer from '../components/FormContainer';
import FullPageLoader from '../components/FullPageLoader';
import { loginWithGoogle } from '../actions/userActions';
import { GOOGLE_CLIENT_ID } from '../constants/appConstants';

const LoginScreen = (props) => {
  const [userNameOrEmail, setUserNameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const dispatch = useDispatch();
  const userLogin = useSelector((state) => state.userLogin);
  const { loading, error, userInfo } = userLogin;

  const redirect = props.location.search ? props.location.search.substring(props.location.search.indexOf('=') + 1) : '/';

  useEffect(() => {
    if (userInfo) {
      props.history.push(redirect);
    }
  }, [props.history, userInfo, redirect]);

  useEffect(() => {
    if (!GOOGLE_CLIENT_ID) {
      return undefined;
    }

    let attempts = 0;
    const initializeGoogleSignIn = () => {
      if (!window.google || !window.google.accounts || !window.google.accounts.id) {
        attempts += 1;
        return attempts < 50;
      }

      window.google.accounts.id.initialize({
        client_id: GOOGLE_CLIENT_ID,
        callback: (response) => dispatch(loginWithGoogle(response.credential))
      });
      window.google.accounts.id.renderButton(document.getElementById('google-signin-button'), {
        theme: 'outline',
        size: 'large',
        width: 320,
        text: 'continue_with'
      });
      return false;
    };

    const intervalId = window.setInterval(() => {
      if (!initializeGoogleSignIn()) {
        window.clearInterval(intervalId);
      }
    }, 100);

    return () => window.clearInterval(intervalId);
  }, [dispatch]);

  const loginSubmitHandler = (e) => {
    e.preventDefault();
    dispatch(login(userNameOrEmail, password));
  };

  return (
    <div>
      <FormContainer>
        <h1>Sign In</h1>
        {error && <Message variant='danger'>{JSON.stringify(error)}</Message>}
        <Form onSubmit={loginSubmitHandler}>
          <Form.Group controlId='userNameOrEmail'>
            <Form.Label>Email Address</Form.Label>
            <Form.Control
              placeholder='Username or Email'
              value={userNameOrEmail}
              onChange={(e) => setUserNameOrEmail(e.target.value)}
            ></Form.Control>
          </Form.Group>

          <Form.Group controlId='password'>
            <Form.Label>Password</Form.Label>
            <Form.Control
              placeholder='Password'
              type='password'
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            ></Form.Control>
          </Form.Group>

          <Button type='submit' variant='primary'>
            Sign In
          </Button>
        </Form>

        {GOOGLE_CLIENT_ID && (
          <>
            <div className='text-center py-3'>or</div>
            <div id='google-signin-button' className='d-flex justify-content-center'></div>
          </>
        )}

        <Row className='py-3'>
          <Col>
            New Customer? <Link to={redirect ? `/register?redirect=${redirect}` : '/register'}>Register</Link>
          </Col>
        </Row>
      </FormContainer>
      {loading && <FullPageLoader></FullPageLoader>}
    </div>
  );
};

export default LoginScreen;
