export interface Organizer {
  idUser: number
  displayName: string
  email: string
  name: string
  surname: string
  address: string
  phone: string
  userEnabled: boolean
  roleId: number | null
  legalName: string
  cif: string
  organizerEnabled: boolean
}
