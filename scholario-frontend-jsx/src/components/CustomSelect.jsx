import React from 'react';
import { 
  FormControl, 
  InputLabel, 
  Select, 
  MenuItem, 
  Typography,
  Box
} from '@mui/material';

export const CustomSelect = ({ label, options, value, onChange, placeholder }) => {
  const handleChange = (event) => {
    onChange(event.target.value);
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, width: '100%' }}>
      <Typography 
        variant="caption" 
        fontWeight="black" 
        color="text.secondary" 
        sx={{ textTransform: 'uppercase', letterSpacing: 1.5, fontSize: 10, pl: 0.5 }}
      >
        {label}
      </Typography>
      <FormControl fullWidth size="medium">
        <Select
          value={value}
          onChange={handleChange}
          displayEmpty
          sx={{
            borderRadius: 3,
            bgcolor: 'grey.50',
            '& .MuiOutlinedInput-notchedOutline': {
              borderColor: 'transparent',
              borderWidth: 2,
            },
            '&:hover .MuiOutlinedInput-notchedOutline': {
              borderColor: 'divider',
            },
            '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
              borderColor: 'primary.main',
            },
            fontWeight: 600,
            fontSize: 14,
          }}
        >
          {placeholder && (
            <MenuItem value="" disabled>
              <Typography color="text.secondary" sx={{ fontSize: 14, fontWeight: 500 }}>
                {placeholder}
              </Typography>
            </MenuItem>
          )}
          {options.map((option) => (
            <MenuItem 
              key={option.id} 
              value={option.id}
              sx={{
                fontSize: 14,
                fontWeight: 600,
                py: 1.5,
                px: 2,
                my: 0.5,
                mx: 1,
                borderRadius: 2,
                '&.Mui-selected': {
                  bgcolor: 'primary.lighter',
                  color: 'primary.main',
                  fontWeight: 700,
                  '&:hover': {
                    bgcolor: 'primary.lighter',
                  }
                }
              }}
            >
              {option.name}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    </Box>
  );
};
