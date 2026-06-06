<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const email = ref('')
const password = ref('')
const error = ref('')

const submit = async () => {
  error.value = ''
  if (!email.value || !password.value) {
    error.value = 'Email et mot de passe requis.'
    return
  }

  try {
    await authStore.login({ email: email.value, password: password.value })
    router.push('/compose')
  } catch (err) {
    error.value = 'Échec de la connexion.'
  }
}
</script>

<template>
  <section class="mx-auto max-w-md rounded-3xl border border-sky-200 bg-sky-100 p-8 shadow-xl shadow-blue-900/10">
    <h1 class="text-3xl font-semibold text-slate-950">Connexion</h1>
    <p class="mt-2 text-sm text-slate-500">Connectez-vous pour accéder à votre espace Notify.</p>

    <div class="mt-8 space-y-5">
      <div>
        <label class="mb-2 block text-sm font-medium text-slate-700">Email</label>
        <input
          v-model="email"
          type="email"
          placeholder="demo@notify.local"
          class="w-full rounded-3xl border border-sky-200 bg-white px-4 py-3 text-slate-950 outline-none transition focus:border-sky-600"
        />
      </div>

      <div>
        <label class="mb-2 block text-sm font-medium text-slate-700">Mot de passe</label>
        <input
          v-model="password"
          type="password"
          placeholder="••••••••"
          class="w-full rounded-3xl border border-sky-200 bg-white px-4 py-3 text-slate-950 outline-none transition focus:border-sky-600"
        />
      </div>

      <div v-if="error" class="rounded-3xl bg-rose-50 px-4 py-3 text-sm text-rose-700">{{ error }}</div>

      <button
        @click="submit"
        class="w-full rounded-full bg-sky-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-sky-700"
      >
        Se connecter
      </button>
    </div>
  </section>
</template>
