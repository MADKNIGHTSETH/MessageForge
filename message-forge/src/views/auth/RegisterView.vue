<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const email = ref('')
const password = ref('')
const displayName = ref('')
const error = ref('')

const register = async () => {
  error.value = ''
  if (!email.value || !password.value || !displayName.value) {
    error.value = 'Tous les champs sont requis.'
    return
  }

  await authStore.register({
    email: email.value,
    password: password.value,
    displayName: displayName.value,
  })
  router.push('/compose')
}
</script>

<template>
  <section class="mx-auto max-w-xl rounded-3xl border border-sky-200 bg-sky-100 p-8 shadow-lg shadow-blue-900/10">
    <h1 class="text-2xl font-semibold text-slate-950">Inscription</h1>
    <p class="mt-2 text-sm text-slate-500">Cr&eacute;ez un compte de d&eacute;monstration pour Notify.</p>

    <div class="mt-8 space-y-4">
      <label class="block text-sm font-medium text-slate-700">Nom affich&eacute;</label>
      <input
        type="text"
        v-model="displayName"
        placeholder="Ex. Jean Dupont"
        class="w-full rounded-3xl border border-sky-200 bg-white px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-sky-600 focus:bg-sky-50"
      />

      <label class="block text-sm font-medium text-slate-700">Email</label>
      <input
        type="email"
        v-model="email"
        placeholder="demo@notify.local"
        class="w-full rounded-3xl border border-sky-200 bg-white px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-sky-600 focus:bg-sky-50"
      />

      <label class="block text-sm font-medium text-slate-700">Mot de passe</label>
      <input
        type="password"
        v-model="password"
        placeholder="********"
        class="w-full rounded-3xl border border-sky-200 bg-white px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-sky-600 focus:bg-sky-50"
      />

      <div v-if="error" class="rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700">{{ error }}</div>

      <button
        type="button"
        @click="register"
        class="w-full rounded-full bg-sky-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-sky-700"
      >
        Cr&eacute;er un compte
      </button>
    </div>

    <p class="mt-6 text-sm text-slate-500">
      D&eacute;j&agrave; inscrit ?
      <RouterLink to="/login" class="font-semibold text-sky-700 hover:text-sky-800">Se connecter</RouterLink>
    </p>
  </section>
</template>
