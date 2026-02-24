<script setup lang="ts">
import { useTheme } from 'vuetify'

const theme = useTheme()

</script>

<template>
        <!-- Toggle between Light / Dark -->
        <v-btn
            :icon="theme.name.value === 'dark' ?  'mdi-weather-night' : 'mdi-white-balance-sunny'"
            @click="theme.toggle()"
            text="Toggle Light / Dark"
            v-tooltip:bottom="`Tema Claro/Escuro`"
        ></v-btn>
        <!-- Change to a specific theme -->
<!--        <v-btn-->
<!--            @click="theme.change('dark')"-->
<!--            text="Change to Dark"-->
<!--        ></v-btn>-->

<!--        &lt;!&ndash; Cycle between all themes &ndash;&gt;-->
<!--        <v-btn-->
<!--            @click="theme.cycle()"-->
<!--            text="Cycle All Themes"-->
<!--        ></v-btn>-->

<!--        &lt;!&ndash; Cycle between specific themes &ndash;&gt;-->
<!--        <v-btn-->
<!--            @click="theme.cycle(['custom', 'light', 'system'])"-->
<!--            text="Cycle Specific Themes"-->
<!--        ></v-btn>-->
</template>


<style scoped>

</style>