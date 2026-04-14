export interface ProfileFormState {
  name: string
  surname: string
  email: string
  address: string
  phone: string
  legalName: string
  cif: string
  password: string
  passwordConfirmation: string
}

export interface LapTimeFormState {
  trackId: string
  lapDate: string
  lapTimeText: string
  vehicle: string
}
