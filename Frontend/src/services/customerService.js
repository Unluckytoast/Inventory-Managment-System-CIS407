import api from './api';

export const customerService = {
    login: async (email) => {
        try {
            const response = await api.post('/customers/login', { email });
            return response.data;
        } catch (error) {
            throw error.response?.data || 'Login failed';
        }
    },

    addCustomer: async (customer) => {
        try {
            const response = await api.post('/customers', customer);
            return response.data;
        } catch (error) {
            throw error.response?.data || 'Failed to add customer';
        }
    },
};
