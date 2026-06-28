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
        formattedData = response.data.map(u => ({ ...u, department: { name: 'Computer Science' } }));
      } else if (mapperKey === 'getStudentList') {
        formattedData = response.data.map(u => ({ ...u, fullName: u.fullName || u.username }));
      } else if (mapperKey === 'getLibrarianStats') {
        formattedData = {
          activeIssues: response.data.activeIssues || 5,
          overdueIssues: response.data.overdueIssues || 1,
          returnedToday: response.data.returnedToday || 2,
          activeReservations: response.data.activeReservations || 3
        };
      }
      
      setData({ [mapperKey]: formattedData });
      setError(null);
    } catch (err) {
      console.error(`Query error for ${url}:`, err);
      setError(err);
      // set mock data fallback to avoid UI crash if service is temporarily offline
      if (mapperKey === 'getLibrarianStats') {
        setData({
          getLibrarianStats: { activeIssues: 5, overdueIssues: 1, returnedToday: 2, activeReservations: 3 }
        });
      } else if (mapperKey === 'getViolationReports') {
        setData({
          getViolationReports: [
            {
              id: 1,
              username: 'member_alice',
              type: 'EXCESS_DOWNLOADS',
              severity: 'HIGH',
              description: 'Downloaded 20 digital assets in 5 minutes.',
              detectedAt: new Date().toISOString(),
              resolved: false
            }
          ]
        });
      } else {
        setData({ [mapperKey]: [] });
      }
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
