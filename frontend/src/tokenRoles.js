// UI hint only: decoding is NOT signature verification or server authorization.
export function hasRole(token, role) {
  try {
    if (typeof token !== 'string' || token.split('.').length !== 3) return false;
    const part = token.split('.')[1];
    const base64 = part.replace(/-/g, '+').replace(/_/g, '/');
    const claims = JSON.parse(atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')));
    return Array.isArray(claims?.roles) && claims.roles.includes(role);
  } catch { return false; }
}
