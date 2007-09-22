--
--	aram.vhd
--
--	internal memory for JOP3
--	Version for Actel
--
--	2006-08-10	signal for inverted clock
--
--

library IEEE ;
use IEEE.std_logic_1164.all;
use IEEE.numeric_std.all;

entity ram is
  generic (
    width       : integer := 32;
    addr_width  : integer := 8);
  port (
    reset       : in std_logic;
    data	: in std_logic_vector(width-1 downto 0);
    wraddress	: in std_logic_vector(addr_width-1 downto 0);
    rdaddress	: in std_logic_vector(addr_width-1 downto 0);
    wren        : in std_logic;
    clock	: in std_logic;
    q		: out std_logic_vector(width-1 downto 0));
end ram;

--
--	registered and delayed wraddress, wren
--	registered din
--	registered rdaddress
--	unregistered dout
--
--	with normal clock on wrclock:
--		=> read during write on same address!!! (ok in ACEX)
--	for Cyclone use not clock for wrclock, but works also on ACEX
--
architecture rtl of ram is

  signal wren_dly	: std_logic;
  signal wrdata         : std_logic_vector(width-1 downto 0);
  signal wraddr_dly	: std_logic_vector(addr_width-1 downto 0);

  signal nclk		: std_logic;

  signal reset_dly      : std_logic;
  signal init_data      : std_logic_vector(width-1 downto 0);
  signal init_cnt       : unsigned(addr_width-1 downto 0);
  
  component actelram_block is 
    port(
      WD                : in std_logic_vector(width-1 downto 0);
      RD                : out std_logic_vector(width-1 downto 0);
      WEN, REN          : in std_logic; 
      WADDR             : in std_logic_vector(addr_width-1 downto 0);
      RADDR             : in std_logic_vector(addr_width-1 downto 0);
      WCLK, RCLK        : in std_logic
      );
  end component;

  component actelram_initrom
    generic (
      width      : integer;
      addr_width : integer);
    port (
      address    : in  std_logic_vector(addr_width-1 downto 0);
      data       : out std_logic_vector(width-1 downto 0));
  end component;

begin

  nclk <= not clock;

--
--	delay wr addr and ena because of registerd indata
--

  process (clock, reset, reset_dly)
  begin

    if rising_edge(clock) then

      if reset = '1' and reset_dly /= reset then

        init_cnt <= (others => '0');
      
      elsif reset = '1' then

        if init_cnt /= X"FF" then        
          init_cnt <= init_cnt + 1;
        end if;

        wren_dly <= '1';
        wrdata <= init_data;
        wraddr_dly <= init_cnt;
        
      else
        
        wren_dly <= wren;
        wrdata <= data;
        wraddr_dly <= wraddress;
        
      end if;

      reset_dly <= reset;
      
    end if;
    
  end process;

  cmp_initrom : actelram_initrom
    generic map (
      width => width,
      addr_width => addr_width)
    port map (
      address => std_logic_vector(init_cnt),
      data => init_data);
  
  cmp_ram : actelram_block
    port map (
      WD => wrdata,
      RD => q,
      WEN => wren_dly,
      REN => '1',
      WADDR => wraddr_dly,
      RADDR => rdaddress,
      WCLK => nclk,
      RCLK => clock
      );
  
end rtl;

