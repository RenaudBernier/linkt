import React, { useState } from "react";
import {
  AppBar,
  Toolbar,
  Box,
  TextField,
  InputAdornment,
  IconButton,
  Menu,
  MenuItem,
  Avatar,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import PersonIcon from "@mui/icons-material/Person";
import logo from "../assets/logo.png";

const Header: React.FC = () => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  {/* Gotta handle functions at the start! */}
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleSettings = () => {
    console.log("Settings clicked");
    handleClose();
  };

  const handleLogout = () => {
    console.log("Logout clicked");
    handleClose();
  };

  return (
    <AppBar
      sx={{
        backgroundColor: "var(--charcoal)",
        boxShadow: "none",
        borderBottom: "1px solid var(--charcoal-25)",
      }}
    >
      <Toolbar sx={{ justifyContent: "space-between", px: 3 }}>
        {/* Logo */}
        {/* sx = applies CUSTOM STYLES to stuff */}
        <Box sx={{ display: "flex", alignItems: "center" }}>
          <img src={logo} alt="Logo" style={{ height: "60px", width: "auto" }}
          />
        </Box>

        {/* Search Bar */}
        <Box sx={{ flexGrow: 1, maxWidth: "600px", mx: 4 }}>
          <TextField
            fullWidth
            placeholder="Search for events..."
            variant="outlined"
            size="small"
            sx={{
              "& .MuiOutlinedInput-root": {
                backgroundColor: "white",
                borderRadius: "25px",
                "& fieldset": {
                  borderColor: "var(--charcoal-25)",
                },
                "&:hover fieldset": {
                  borderColor: "var(--fluorescent-cyan)",
                },
                "&.Mui-focused fieldset": {
                  borderColor: "var(--fluorescent-cyan)",
                },
              },
            }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon sx={{ color: "var(--charcoal-50)" }} />
                </InputAdornment>
              ),
            }}
          />
        </Box>

        {/* User Menu */}
        <Box>
          <IconButton
            onClick={handleClick}
            sx={{
              color: "white",
              "&:hover": {
                backgroundColor: "var(--charcoal-25)",
              },
            }}
          >
            <Avatar
              sx={{
                width: 32,
                height: 32,
                backgroundColor: "var(--fluorescent-cyan)",
                color: "var(--charcoal)",
              }}
            >
              <PersonIcon />
            </Avatar>
          </IconButton>
          <Menu
            anchorEl={anchorEl}
            open={open}
            onClose={handleClose}
            anchorOrigin={{
              vertical: "bottom",
              horizontal: "right",
            }}
            transformOrigin={{
              vertical: "top",
              horizontal: "right",
            }}
            sx={{
              "& .MuiPaper-root": {
                backgroundColor: "white",
                borderRadius: "8px",
                boxShadow: "0 4px 12px var(--charcoal-25)",
                border: "1px solid var(--charcoal-10)",
              },
            }}
          >
            <MenuItem
              onClick={handleSettings}
              sx={{
                "&:hover": {
                  backgroundColor: "var(--fluorescent-cyan-10)",
                },
              }}
            >
              Settings
            </MenuItem>
            <MenuItem
              onClick={handleLogout}
              sx={{
                "&:hover": {
                  backgroundColor: "var(--amaranth-purple-10)",
                },
              }}
            >
              Log Out
            </MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
