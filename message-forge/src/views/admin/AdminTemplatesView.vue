<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Trash2 } from '@lucide/vue'
import { useAdminStore } from '../../stores/admin'

const adminStore = useAdminStore()
const templates = computed(() => adminStore.templates)

const isCreating = ref(false)
const newTemplate = ref({
  name: '',
  channelType: 'EMAIL',
  templateBody: '',
  isDefault: false
})

onMounted(() => {
  adminStore.fetchTemplates()
})

const submitTemplate = async () => {
  if (!newTemplate.value.name || !newTemplate.value.templateBody) return
  await adminStore.createTemplate(newTemplate.value)
  isCreating.value = false
  newTemplate.value = { name: '', channelType: 'EMAIL', templateBody: '', isDefault: false }
}
</script>

<template>
  <section class="rounded-3xl border border-sky-200 bg-sky-100 p-6 shadow-lg shadow-blue-900/10">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-semibold text-slate-950">Templates système</h2>
        <p class="mt-2 text-sm text-slate-600">Gérer les formats par défaut injectés dans les messages.</p>
      </div>
      <button 
        @click="isCreating = true"
        class="inline-flex items-center gap-2 rounded-full bg-sky-600 px-4 py-2 text-sm font-semibold text-white hover:bg-sky-700"
      >
        <Plus class="h-4 w-4" /> Ajouter
      </button>
    </div>

    <!-- Create Form -->
    <div v-if="isCreating" class="mt-6 rounded-3xl border border-sky-200 bg-white p-5 shadow-sm">
      <h3 class="text-lg font-semibold text-slate-900 mb-4">Nouveau template</h3>
      <div class="grid gap-4 sm:grid-cols-2">
        <div>
          <label class="block text-sm font-medium text-slate-700">Nom</label>
          <input v-model="newTemplate.name" type="text" class="mt-1 w-full rounded-2xl border px-3 py-2 text-sm outline-none focus:border-sky-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700">Canal</label>
          <select v-model="newTemplate.channelType" class="mt-1 w-full rounded-2xl border px-3 py-2 text-sm outline-none focus:border-sky-500">
            <option value="EMAIL">Email</option>
            <option value="SMS">SMS</option>
            <option value="LINKEDIN">LinkedIn</option>
            <option value="SLACK">Slack</option>
          </select>
        </div>
      </div>
      <div class="mt-4">
        <label class="block text-sm font-medium text-slate-700">Contenu ({content} sera remplacé par le texte)</label>
        <textarea v-model="newTemplate.templateBody" rows="4" class="mt-1 w-full rounded-2xl border px-3 py-2 text-sm outline-none focus:border-sky-500"></textarea>
      </div>
      <div class="mt-4 flex items-center gap-2">
        <input type="checkbox" v-model="newTemplate.isDefault" id="isDefault" class="h-4 w-4 rounded border-slate-300 text-sky-600 focus:ring-sky-600">
        <label for="isDefault" class="text-sm text-slate-700">Définir comme template par défaut pour ce canal</label>
      </div>
      <div class="mt-6 flex justify-end gap-2">
        <button @click="isCreating = false" class="rounded-full px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100">Annuler</button>
        <button @click="submitTemplate" class="rounded-full bg-sky-600 px-4 py-2 text-sm font-semibold text-white hover:bg-sky-700">Enregistrer</button>
      </div>
    </div>

    <!-- Template List -->
    <div v-if="adminStore.isLoading && !templates.length" class="mt-6 text-sm text-slate-500">Chargement...</div>
    
    <div class="mt-6 grid gap-4 lg:grid-cols-2">
      <div v-for="t in templates" :key="t.id" class="rounded-3xl border border-sky-100 bg-white p-5 shadow-sm relative">
        <div class="flex justify-between items-start">
          <div>
            <div class="flex items-center gap-2">
              <span class="text-xs font-bold uppercase tracking-wider text-sky-700 bg-sky-100 px-2 py-0.5 rounded-full">{{ t.channelType }}</span>
              <span v-if="t.isDefault" class="text-xs font-bold uppercase tracking-wider text-emerald-700 bg-emerald-100 px-2 py-0.5 rounded-full">Défaut</span>
            </div>
            <h3 class="mt-2 text-lg font-semibold text-slate-900">{{ t.name }}</h3>
          </div>
          <button @click="adminStore.deleteTemplate(t.id)" class="text-slate-400 hover:text-rose-600">
            <Trash2 class="h-5 w-5" />
          </button>
        </div>
        <pre class="mt-4 whitespace-pre-wrap rounded-2xl bg-slate-50 p-3 text-xs text-slate-700">{{ t.templateBody }}</pre>
      </div>
    </div>
  </section>
</template>
