import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '../pages/HomePage.vue';
import LoveChatPage from '../pages/LoveChatPage.vue';
import SuperAgentPage from '../pages/SuperAgentPage.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
    },
    {
      path: '/love',
      name: 'love',
      component: LoveChatPage,
    },
    {
      path: '/super-agent',
      name: 'super-agent',
      component: SuperAgentPage,
    },
  ],
});

export default router;
