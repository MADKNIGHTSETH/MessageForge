<script setup>
import { computed, onMounted } from 'vue'
import { Download, RefreshCw } from '@lucide/vue'
import { useAdminStore } from '../../stores/admin'

const adminStore = useAdminStore()
const logs = computed(() => adminStore.auditLogs)

onMounted(() => {
  adminStore.fetchAuditLogs()
})
</script>

<template>
  <section class="rounded-3xl border border-sky-200 bg-sky-100 p-6 shadow-lg shadow-blue-900/10">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h2 class="text-2xl font-semibold text-slate-950">Journaux d'audit</h2>
        <p class="text-sm text-slate-600">Consultation des actions administrateur et export CSV.</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <button
          type="button"
          @click="adminStore.fetchAuditLogs"
          class="inline-flex items-center justify-center gap-2 rounded-full border border-sky-200 bg-white px-4 py-2 text-sm font-semibold text-sky-800 transition hover:bg-sky-50"
        >
          <RefreshCw class="h-4 w-4" />
          Actualiser
        </button>
        <button
          type="button"
          @click="adminStore.exportAuditLogsToCSV"
          class="inline-flex items-center justify-center gap-2 rounded-full bg-sky-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-sky-700"
        >
          <Download class="h-4 w-4" />
          Exporter en CSV
        </button>
      </div>
    </div>

    <div class="mt-6 overflow-hidden rounded-3xl border border-sky-200 bg-white">
      <table class="w-full min-w-[860px] text-left text-sm">
        <thead class="bg-sky-50 text-xs uppercase tracking-[0.15em] text-sky-800">
          <tr>
            <th class="px-5 py-4">Date</th>
            <th class="px-5 py-4">Admin</th>
            <th class="px-5 py-4">Action</th>
            <th class="px-5 py-4">Details</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-sky-100">
          <tr v-if="adminStore.isLoading">
            <td colspan="4" class="px-5 py-6 text-center text-slate-500">Chargement des journaux...</td>
          </tr>
          <tr v-for="log in logs" :key="log.id" class="align-top text-slate-700">
            <td class="px-5 py-4 whitespace-nowrap">{{ new Date(log.createdAt).toLocaleString('fr-FR') }}</td>
            <td class="px-5 py-4 font-medium text-slate-950">{{ log.userEmail }}</td>
            <td class="px-5 py-4">
              <span class="rounded-full bg-sky-200 px-3 py-1 text-xs font-semibold text-sky-800">{{ log.action }}</span>
            </td>
            <td class="px-5 py-4">
              <div class="space-y-1">
                <div v-if="log.entityType" class="text-[10px] text-slate-500 uppercase tracking-wider">
                  {{ log.entityType }}: {{ log.entityId }}
                </div>
                <pre v-if="log.details" class="whitespace-pre-wrap rounded-2xl bg-sky-50 p-3 text-xs text-slate-700">{{ JSON.stringify(log.details, null, 2) }}</pre>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
