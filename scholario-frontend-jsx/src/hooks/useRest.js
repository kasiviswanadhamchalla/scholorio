import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

// Helper to configure authorization headers from Keycloak/Local storage
const getHeaders = () => {
  // Try Keycloak token first, otherwise fallback to local/mock token
  const token = window.localStorage.getItem('scholario_token') || 'mock-jwt-token-123456';
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
  };
};

export const useRestQuery = (url, mapperKey, params = null) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const response = await axios.get(url, {
        headers: getHeaders(),
        params: params || undefined
      });
      
      // Formatting to match GraphQL response shape expected by component
      let formattedData = response.data;
      if (mapperKey === 'getFacultyList') {
        formattedData = response.data.filter(u => u.roles.includes('MEMBER'));
      } else if (mapperKey === 'getStudentList') {
        formattedData = response.data
          .filter(u => u.roles.includes('MEMBER'))
          .map(u => ({ ...u, fullName: u.fullName || u.username }));
      } else if (mapperKey === 'getLibrarianStats') {
        formattedData = {
          activeIssues: response.data.activeIssues ?? 0,
          overdueIssues: response.data.overdueIssues ?? 0,
          returnedToday: response.data.returnedToday ?? 0,
          activeReservations: response.data.activeReservations ?? 0
        };
      }
      
      setData({ [mapperKey]: formattedData });
      setError(null);
    } catch (err) {
      console.error(`Query error for ${url}:`, err);
      setError(err);
      setData({ [mapperKey]: [] });
    } finally {
      setLoading(false);
    }
  }, [url, mapperKey, JSON.stringify(params)]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return { data, loading, error, refetch: fetchData };
};

export const useRestMutation = (urlFunc, method = 'POST', mapperKey) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const mutate = useCallback(async (variablesWrapper) => {
    setLoading(true);
    const variables = variablesWrapper?.variables || {};
    const url = typeof urlFunc === 'function' ? urlFunc(variables) : urlFunc;
    
    let body = variables.input || variables;
    
    try {
      const response = await axios({
        method,
        url,
        data: body,
        headers: getHeaders()
      });
      setError(null);
      return { data: { [mapperKey]: response.data } };
    } catch (err) {
      console.error(`Mutation error for ${url}:`, err);
      setError(err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [urlFunc, method, mapperKey]);

  return [mutate, { loading, error }];
};
