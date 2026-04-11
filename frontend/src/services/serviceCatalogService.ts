import { api } from '@/services/api'
import type { ServiceCatalogItem, ServiceCatalogPayload } from '@/types/serviceCatalog'

export async function getServices(): Promise<ServiceCatalogItem[]> {
  const response = await api.get<ServiceCatalogItem[]>('/services')
  return response.data
}

export async function createServiceCatalogItem(
  payload: ServiceCatalogPayload,
): Promise<ServiceCatalogItem> {
  const response = await api.post<ServiceCatalogItem>('/services', payload)
  return response.data
}

export async function updateServiceCatalogItem(
  id: number,
  payload: ServiceCatalogPayload,
): Promise<ServiceCatalogItem> {
  const response = await api.put<ServiceCatalogItem>(`/services/${id}`, payload)
  return response.data
}

export async function enableServiceCatalogItem(id: number): Promise<ServiceCatalogItem> {
  const response = await api.patch<ServiceCatalogItem>(`/services/${id}/enable`)
  return response.data
}

export async function disableServiceCatalogItem(id: number): Promise<ServiceCatalogItem> {
  const response = await api.patch<ServiceCatalogItem>(`/services/${id}/disable`)
  return response.data
}
