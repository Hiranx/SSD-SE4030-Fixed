import jwtDecode from 'jwt-decode';

export const getErrorMessage = (error) => {
  if (!error) {
    return 'Something went wrong';
  }

  const responseData = error.response && error.response.data;
  if (typeof responseData === 'string' && responseData.trim()) {
    return responseData;
  }
  if (responseData && responseData.error_description) {
    return responseData.error_description;
  }
  if (responseData && Array.isArray(responseData.errors) && responseData.errors.length > 0) {
    return responseData.errors[0].message || responseData.errors[0].defaultMessage || 'Request failed';
  }
  if (responseData && responseData.message) {
    return responseData.message;
  }

  return error.message || 'Something went wrong';
};

export const isAdmin = () => {
  const userInfoLocalStorage = localStorage.getItem('userInfo');
  if (userInfoLocalStorage) {
    const token = JSON.parse(userInfoLocalStorage).token;
    let decodedToken = jwtDecode(token);
    return decodedToken?.authorities?.includes('ADMIN_USER');
  }
  return false;
};
