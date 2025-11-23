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
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import logo from "../assets/logo.png";

const Header: React.FC = () => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const navigate = useNavigate();
  const { isAuthenticated, logout, user } = useAuth();
  const [headerSearch, setHeaderSearch] = useState<string>("");

  {/* Gotta handle functions at the start! */}
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleSettings = () => {
    navigate('/settings');
    handleClose();
  };

  const handleMyTickets = () => {
    navigate('/mytickets');
    handleClose();
  };

  const handleSavedEvents = () => {
    navigate('/savedtickets');
    handleClose();
  };

  const handleMyEvents = () => {
    navigate('/my-events');
    handleClose();
  };

  const handleAdmin = () => {
    navigate('/admin/approve-organizer');
    handleClose();
  };

  const handleAdminApproveEvents = () => {
    navigate('/admin/approve-events');
    handleClose();
  };

  const handleAdminDashboard = () => {
    navigate('/admin/dashboard');
    handleClose();
  };

  const handleLogout = () => {
    logout();
    handleClose();
    navigate('/');
  };

  const handleLogin = () => {
    navigate('/login');
    handleClose();
  };

  const handleSignUp = () => {
    navigate('/signup');
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
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            cursor: "pointer"
          }}
          onClick={() => navigate("/")

          }
        >
          <img src={logo} alt="Logo" style={{ height: "90px", width: "auto" }}
          />
        </Box>

        {/* Search Bar */}
        <Box sx={{ flexGrow: 1, maxWidth: "600px", mx: 4 }}>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              const trimmed = headerSearch.trim();
              if (trimmed.length === 0) {
                navigate('/events');
              } else {
                navigate(`/events?search=${encodeURIComponent(trimmed)}`);
              }
            }}
          >
            <TextField
              fullWidth
              placeholder="Search for events..."
              variant="outlined"
              size="small"
              value={headerSearch}
              onChange={(e) => setHeaderSearch(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Escape') {
                  setHeaderSearch('');
                }
              }}
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
                    <IconButton
                      type="submit"
                      size="small"
                      sx={{ p: 0, mr: 0.5 }}
                      aria-label="search events"
                    >
                      <SearchIcon sx={{ color: "var(--charcoal-50)" }} />
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
          </form>
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
            {isAuthenticated ? [


              user && (user.userType === 'student' || user.userType === 'organizer') && (
                <MenuItem
                  key="tickets"
                  onClick={handleMyTickets}
                  sx={{
                    "&:hover": {
                      backgroundColor: "var(--fluorescent-cyan-10)",
                    },
                  }}
                >
                  My Tickets
                </MenuItem>
              ),


              user && (user.userType === 'student' || user.userType === 'organizer') && (
                <MenuItem
                  key="saved-events"
                  onClick={handleSavedEvents}
                  sx={{
                    "&:hover": {
                      backgroundColor: "var(--fluorescent-cyan-10)",
                    },
                  }}
                >
                  Saved Events
                </MenuItem>
              ),

              user && user.userType === 'organizer' && (
                <MenuItem
                  key="my-events"
                  onClick={handleMyEvents}
                  sx={{
                    "&:hover": {
                      backgroundColor: "var(--fluorescent-cyan-10)",
                    },
                  }}
                >
                  My Events
                </MenuItem>
              ),

              user && user.userType === 'administrator' && (
                <MenuItem
                  key="admin-dashboard"
                  onClick={handleAdminDashboard}
                  sx={{
                    "&:hover": {
                      backgroundColor: "var(--fluorescent-cyan-10)",
                    },
                  }}
                >
                  Admin Dashboard
                </MenuItem>
              ),

              user && user.userType === 'administrator' && (
                <MenuItem
                  key="admin"
                  onClick={handleAdmin}
                  sx={{
                    "&:hover": {
                      backgroundColor: "var(--fluorescent-cyan-10)",
                    },
                  }}
                >
                  Approve Organizers
                </MenuItem>
              ),

              user && user.userType === 'administrator' && (
                <MenuItem
                  key="approve-events"
                  onClick={handleAdminApproveEvents}
                  sx={{
                    "&:hover": {
                      backgroundColor: "var(--fluorescent-cyan-10)",
                    },
                  }}
                >
                  Approve Events
                </MenuItem>
              ),




              <MenuItem
                key="settings"
                onClick={handleSettings}
                sx={{
                  "&:hover": {
                    backgroundColor: "var(--fluorescent-cyan-10)",
                  },
                }}
              >
                Settings
              </MenuItem>,


              <MenuItem
                key="logout"
                onClick={handleLogout}
                sx={{
                  "&:hover": {
                    backgroundColor: "var(--amaranth-purple-10)",
                  },
                }}
              >
                Log Out
              </MenuItem>
            ] : [



              // No user logged in
              <MenuItem
                key="login"
                onClick={handleLogin}
                sx={{
                  "&:hover": {
                    backgroundColor: "var(--fluorescent-cyan-10)",
                  },
                }}
              >
                Login
              </MenuItem>,
              <MenuItem
                key="signup"
                onClick={handleSignUp}
                sx={{
                  "&:hover": {
                    backgroundColor: "var(--fluorescent-cyan-10)",
                  },
                }}
              >
                Sign Up
              </MenuItem>
            ]}
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
