<script setup>
import { computed, onMounted } from 'vue'
import { RefreshCw } from '@lucide/vue'
import { useAdminStore } from '../../stores/admin'

const adminStore = useAdminStore()
const users = computed(() => adminStore.users)

onMounted(() => {
  adminStore.fetchUsers()
})
</script>

<template>
  <section class="rounded-3xl border border-sky-200 bg-sky-100 p-6 shadow-lg shadow-blue-900/10">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h2 class="text-2xl font-semibold text-slate-950">Gestion des comptes utilisateurs</h2>
        <p class="text-sm text-slate-600">Activation et desactivation des comptes selon le diagramme d'administration.</p>
      </div>
      <button
        type="button"
        @click="adminStore.fetchUsers"
        class="inline-flex items-center justify-center gap-2 rounded-full bg-sky-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-sky-700"
      >
        <RefreshCw class="h-4 w-4" />
        Actualiser
      </button>
    </div>

    <div class="mt-6 overflow-hidden rounded-3xl border border-sky-200 bg-white">
      <table class="w-full min-w-[760px] text-left text-sm">
        <thead class="bg-sky-50 text-xs uppercase tracking-[0.15em] text-sky-800">
          <tr>
            <th class="px-5 py-4">Utilisateur</th>
            <th class="px-5 py-4">Email</th>
            <th class="px-5 py-4">Role</th>
            <th class="px-5 py-4">Etat</th>
            <th class="px-5 py-4 text-right">Activation</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-sky-100">
          <tr v-if="adminStore.isLoading">
            <td colspan="5" class="px-5 py-6 text-center text-slate-500">Chargement des utilisateurs...</td>
          </tr>
          <tr v-for="user in users" :key="user.id" class="text-slate-700">
            <td class="px-5 py-4 font-medium text-slate-950">{{ user.displayName || 'Sans nom' }}</td>
            <td class="px-5 py-4">{{ user.email }}</td>
            <td class="px-5 py-4">
              <span class="rounded-full bg-sky-50 px-3 py-1 text-xs font-semibold text-sky-800">{{ user.role }}</span>
            </td>
            <td class="px-5 py-4">
              <span
                :class="[
                  'rounded-full px-3 py-1 text-xs font-semibold',
                  user.isActive ? 'bg-sky-200 text-sky-800' : 'bg-rose-100 text-rose-700',
                ]"
              >
                {{ user.isActive ? 'Actif' : 'Inactif' }}
              </span>
            </td>
            <td class="px-5 py-4 text-right">
              <button
                type="button"
                role="switch"
                :aria-checked="user.isActive"
                @click="adminStore.toggleUserStatus(user.id, user.isActive)"
                :class="[
                  'relative inline-flex h-7 w-12 items-center rounded-full transition',
                  user.isActive ? 'bg-sky-600' : 'bg-slate-300',
                ]"
              >
                <span
                  :class="[
                    'inline-block h-5 w-5 rounded-full bg-white shadow transition',
                    user.isActive ? 'translate-x-6' : 'translate-x-1',
                  ]"
                />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
