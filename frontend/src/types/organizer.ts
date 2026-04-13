export interface Organizer {
  idUser: number
  displayName: string
  email: string
  name: string
  surname: string
  address: string
  phone: string
  created?: string | null
  userEnabled: boolean
  roleId: number | null
  roleName?: string | null
  legalName: string
  cif: string
  organizerEnabled: boolean
}

export interface OrganizerProfile extends Organizer {
  created: string | null
  roleName: string | null
}

export interface UpdateCurrentOrganizerProfilePayload {
  name: string
  surname: string
  email: string
  address: string
  phone: string
  legalName: string
  cif: string
  password?: string
}
