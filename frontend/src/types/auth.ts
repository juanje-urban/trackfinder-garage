export interface AuthCredentials {
  email: string
  password: string
}

export interface AuthRegisterPayload extends AuthCredentials {
  displayName: string
  name: string
  surname: string
  address: string
  phone: string
}

export interface AuthOrganizerRegisterPayload extends AuthRegisterPayload {
  legalName: string
  cif: string
}

export interface AuthIdentity {
  userId: number
  displayName: string
  email: string
  roleName: string | null
}

export interface AuthSession extends AuthIdentity {
  authorizationHeader: string
}
